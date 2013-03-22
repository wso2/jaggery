var registry = registry || {};

(function (server, registry) {
    var log = new Log();

    var Resource = Packages.org.wso2.carbon.registry.core.Resource;

    var Collection = Packages.org.wso2.carbon.registry.core.Collection;

    var Comment = Packages.org.wso2.carbon.registry.core.Comment;

    var StaticConfiguration = Packages.org.wso2.carbon.registry.core.config.StaticConfiguration;

    var content = function (registry, resource, paging) {
        if (resource instanceof Collection) {
            // #1 : this always sort children by name, so sorting cannot be done for the chunk
            return function (pagination) {
                pagination = pagination || paging;
                return children(registry, resource, pagination);
            };
        }
        if (resource instanceof Comment) {
            return String(resource.getText());
        }
        var stream = resource.getContentStream();
        if (stream) {
            return new Stream(stream);
        }
        return String(resource.content);
    };

    var commentsQuery = function (registry, resource, paging) {
        var query, sort, limit, sorter,
            paged = true,
            database = registry.database;
        if (registry.versioning.comments) {
            query = 'SELECT REG_COMMENT_ID FROM REG_COMMENT C, REG_RESOURCE_COMMENT RC ' +
                'WHERE C.REG_ID=RC.REG_COMMENT_ID AND RC.REG_VERSION=' + resource.versionNumber + ' ' +
                'AND C.REG_TENANT_ID=' + registry.tenant + ' AND RC.REG_TENANT_ID=' + registry.tenant;
        } else {
            if (resource instanceof Collection) {
                query = 'SELECT REG_COMMENT_ID FROM REG_COMMENT C, REG_RESOURCE_COMMENT RC ' +
                    'WHERE C.REG_ID=RC.REG_COMMENT_ID AND RC.REG_PATH_ID=' + resource.id + ' ' +
                    'AND RC.REG_RESOURCE_NAME IS NULL AND C.REG_TENANT_ID=' + registry.tenant + ' ' +
                    'AND RC.REG_TENANT_ID=' + registry.tenant;
            } else {
                query = 'SELECT REG_COMMENT_ID FROM REG_COMMENT C, REG_RESOURCE_COMMENT RC ' +
                    'WHERE C.REG_ID=RC.REG_COMMENT_ID AND RC.REG_PATH_ID=' + resource.id + ' ' +
                    'AND RC.REG_RESOURCE_NAME = ' + resource.name + ' AND C.REG_TENANT_ID=' + registry.tenant + ' ' +
                    'AND RC.REG_TENANT_ID=' + registry.tenant;
            }
        }
        switch (paging.sort) {
            case 'recent' :
            default:
                sort = ' ORDER BY C.REG_COMMENTED_TIME DESC';
                sorter = function (l, r) {
                    return l.created.time < r.created.time;
                };
        }
        switch (database.name) {
            case 'MySQL':
                limit = ' LIMIT ' + paging.count + ' OFFSET ' + paging.start;
                break;
            default :
                paged = false;
                limit = '';
        }
        query += sort + limit;
        return {
            query: query,
            sorter: sorter,
            paged: paged
        };
    };

    var childrenQuery = function (registry, resource, paging) {
        var query, sort, sorter, limit,
            paged = true,
            pathId = resource.pathID,
            database = registry.database;
        query = 'SELECT R.REG_PATH_ID, R.REG_NAME, R.REG_CREATED_TIME AS REG_CREATED_TIME ' +
            'FROM REG_RESOURCE R WHERE ' +
            'R.REG_PATH_ID=' + pathId + ' AND ' +
            'R.REG_TENANT_ID=' + registry.tenant + ' ' +
            'UNION ' +
            'SELECT P.REG_PATH_ID, R.REG_NAME, R.REG_CREATED_TIME AS REG_CREATED_TIME ' +
            'FROM REG_PATH P, REG_RESOURCE R WHERE ' +
            'P.REG_PATH_PARENT_ID=' + pathId + ' AND ' +
            'P.REG_TENANT_ID=' + registry.tenant + ' AND ' +
            'R.REG_PATH_ID=P.REG_PATH_ID AND ' +
            'R.REG_NAME IS NULL AND ' +
            'R.REG_TENANT_ID=' + registry.tenant;

        paging = merge({
            start: 0,
            count: 25,
            sort: 'recent'
        }, paging);
        switch (paging.sort) {
            case 'recent' :
            default:
                sort = ' ORDER BY REG_CREATED_TIME DESC';
                sorter = function (l, r) {
                    return l.created.time < r.created.time;
                };
        }
        switch (database.name) {
            case 'MySQL':
                limit = ' LIMIT ' + paging.count + ' OFFSET ' + paging.start;
                break;
            default :
                paged = false;
                limit = '';
        }
        query += sort + limit;
        return {
            query: query,
            sorter: sorter,
            paged: paged
        };
    };

    var children = function (registry, resource, paging) {
        var o, pathz, length, i, res,
            pathId = resource.pathID,
            resources = [],
            paths = [];
        o = childrenQuery(registry, resource, paging);
        pathz = registry.query({
            query: o.query,
            resultType: 'Resource'
        });
        length = pathz.length;
        for (i = 0; i < length; i++) {
            res = registry.registry.get(pathz[i]);
            if (pathId == res.pathID && !res.name) {
                continue;
            }
            resources.push({
                path: String(res.path),
                created: {
                    time: res.createdTime.time
                }
            });
        }
        //we have to manually sort this due to the bug in registry.getChildren() (#1 above)
        resources.sort(o.sorter);
        length = resources.length;
        for (i = 0; i < length; i++) {
            paths.push(resources[i].path);
        }
        return o.paged ? paths : paths.slice(paging.start, paging.start + paging.count);
    };

    var resource = function (registry, resource) {
        var path = String(resource.path),
            o = {
                created: {
                    author: String(resource.authorUserName),
                    time: resource.createdTime.time
                },
                content: content(registry, resource, {
                    start: 0,
                    count: 10
                }),
                id: String(resource.id),
                version: resource.versionNumber
            };
        if (resource instanceof Comment) {
            return o;
        }
        if (resource instanceof Collection) {
            o.collection = (resource instanceof Collection);
        }
        o.uuid = String(resource.UUID);
        o.path = String(path);
        o.name = String(resource.name) || resolveName(path);
        o.description = String(resource.description);
        o.updated = {
            author: String(resource.lastUpdaterUserName),
            time: resource.lastModified.time
        };
        o.mediaType = String(resource.mediaType);
        o.properties = function () {
            return properties(resource);
        };
        o.aspects = function () {
            return aspects(resource);
        };
        return o;
    };

    var properties = function (resource) {
        var prop,
            properties = resource.properties,
            props = properties.keySet().toArray(),
            length = props.length,
            o = {};
        for (var i = 0; i < length; i++) {
            prop = props[i];
            o[prop] = resource.getPropertyValues(prop).toArray();
        }
        return o;
    };

    var aspects = function (resource) {
        var aspects = resource.getAspects();
        return aspects ? aspects.toArray() : [];
    };

    var resolveName = function (path) {
        path = path.charAt(path.length - 1) === '/' ? path.substring(0, path.length - 1) : path;
        return path.substring(path.lastIndexOf('/') + 1);
    };

    var merge = function (def, options) {
        if (options) {
            for (var op in def) {
                if (def.hasOwnProperty(op)) {
                    def[op] = options[op] || def[op];
                }
            }
        }
        return def;
    };

    var Registry = function (serv, auth) {
        var registryService = server.osgiService('org.wso2.carbon.registry.core.service.RegistryService');
        if (auth.username) {
            this.tenant = server.tenantId({
                domain: auth.domain,
                username: auth.username
            });
            this.registry = registryService.getRegistry(auth.username, auth.password, this.tenant);
            this.username = auth.username;
            this.versioning = {
                comments: StaticConfiguration.isVersioningComments()
            };
            var db = this.registry.getRegistryContext().getDataAccessManager().getDataSource()
                .getConnection().getMetaData();
            this.database = {
                name: String(db.getDatabaseProductName()),
                version: {
                    major: db.getDatabaseMajorVersion(),
                    minor: db.getDatabaseMinorVersion()
                }
            };
        } else {
            throw new Error('Unsupported authentication mechanism : ' + stringify(auth));
        }
        this.server = serv;
    };

    registry.Registry = Registry;

    Registry.prototype.put = function (path, resource) {
        var res;
        if (resource.collection) {
            res = this.registry.newCollection();
        } else {
            res = this.registry.newResource();
            res.content = resource.content || null;
            res.mediaType = resource.mediaType || null;
        }
        res.name = resource.name;
        res.description = resource.description || null;
        res.UUID = resource.uuid || null;

        var values, length, i, ArrayList,
            properties = resource.properties;
        if (properties) {
            ArrayList = java.util.ArrayList;
            for (var name in properties) {
                var list = new ArrayList();
                if (properties.hasOwnProperty(name)) {
                    values = properties[name];
                    values = values instanceof Array ? values : [values];
                    length = values.length;
                    for (i = 0; i < length; i++) {
                        list.add(values[i]);
                    }
                    res.setProperty(name, list);
                }
            }
        }

        var aspects = resource.aspects;
        if (aspects) {
            length = aspects.length;
            for (i = 0; i < length; i++) {
                res.addAspect(aspects[i]);
            }
        }

        this.registry.put(path, res);
    };

    Registry.prototype.remove = function (path) {
        this.registry.delete(path);
    };

    Registry.prototype.move = function (src, dest) {
        this.registry.move(src, dest);
    };

    Registry.prototype.rename = function (current, newer) {
        this.registry.rename(current, newer);
    };

    Registry.prototype.copy = function (src, dest) {
        this.registry.rename(src, dest);
    };

    Registry.prototype.restore = function (path) {
        this.registry.restoreVersion(path);
    };

    Registry.prototype.get = function (path) {
        var res = this.registry.get(path);
        return resource(this, res);
    };

    Registry.prototype.exists = function (path) {
        return this.registry.resourceExists(path);
    };

    Registry.prototype.content = function (path, paging) {
        var resource = this.registry.get(path);
        paging = merge({
            start: 0,
            count: 10,
            sort: 'recent'
        }, paging);
        return content(this, resource, paging);
    };

    Registry.prototype.tags = function (path) {
        var tag, tags, i, length, count, tz,
            tagz = [];
        if (path) {
            tags = this.registry.getTags(path);
            length = tags.length;
            for (i = 0; i < length; i++) {
                tagz.push(String(tags[i].tagName));
            }
            return tagz;
        }

        tz = {};
        tags = this.query({
            name: 'tags',
            query: 'SELECT RT.REG_TAG_ID FROM REG_RESOURCE_TAG RT ORDER BY RT.REG_TAG_ID',
            resultType: 'Tags'
        });
        length = tags.length;
        for (i = 0; i < length; i++) {
            tag = tags[i].split(';')[1].split(':')[1];
            count = tz[tag];
            count = count ? count + 1 : 1;
            tz[tag] = count;
        }
        for (tag in tz) {
            if (tz.hasOwnProperty(tag)) {
                tagz.push({
                    name: String(tag),
                    count: tz[tag]
                });
            }
        }
        return tagz;
    };

    Registry.prototype.tag = function (path, tags) {
        var i, length;
        tags = tags instanceof Array ? tags : [tags];
        length = tags.length;
        for (i = 0; i < length; i++) {
            this.registry.applyTag(path, tags[i]);
        }
    };

    Registry.prototype.untag = function (path, tags) {
        var i, length;
        tags = tags instanceof Array ? tags : [tags];
        length = tags.length;
        for (i = 0; i < length; i++) {
            this.registry.removeTag(path, tags[i]);
        }
    };

    Registry.prototype.associate = function (src, dest, type) {
        this.registry.addAssociation(src, dest, type);
    };

    Registry.prototype.dissociate = function (src, dest, type) {
        this.registry.removeAssociation(src, dest, type);
    };

    Registry.prototype.associations = function (path, type) {
        var i, asso,
            assos = type ? this.registry.getAssociations(path, type) : this.registry.getAllAssociations(path),
            length = assos.length(),
            associations = [];
        for (i = 0; i < length; i++) {
            asso = assos[i];
            associations.push({
                type: String(asso.associationType),
                src: String(asso.sourcePath),
                dest: String(asso.destinationPath)
            });
        }
        return associations;
    };

    Registry.prototype.addProperty = function (path, name, value) {
        var resource = this.registry.get(path);
        resource.addProperty(name, value);
    };

    Registry.prototype.removeProperty = function (path, name, value) {
        var resource = this.registry.get(path);
        (value ? resource.removePropertyValue(name, value) : resource.removeProperty(name));
    };

    Registry.prototype.properties = function (path) {
        var resource = this.registry.get(path);
        return properties(resource);
    };

    Registry.prototype.version = function (path) {
        this.registry.createVersion(path);
    };

    Registry.prototype.versions = function (path) {
        return this.registry.getVersions(path);
    };

    Registry.prototype.unversion = function (path, snapshot) {
        this.registry.removeVersionHistory(path, snapshot);
    };

    Registry.prototype.comment = function (path, comment) {
        this.registry.addComment(path, new Comment(comment));
    };

    /*    Registry.prototype.comments = function (path, paging) {
     var i, comment, length,
     comments = this.registry.getComments(path),
     commentz = [];
     paging = paging || { start: 0, count: 10 };
     length = paging.start + paging.count;
     length = length > comments.length ? comments.length : length;
     for (i = paging.start; i < length; i++) {
     comment = comments[i];
     commentz.push({
     content: comment.getText(),
     created: {
     author: comment.getUser(),
     time: comment.getCreatedTime().getTime()
     },
     path: comment.getCommentPath()
     });
     }
     return commentz;
     };*/

    Registry.prototype.comments = function (path, paging) {
        var o, ids, i, length,
            comments = [],
            resource = this.registry.get(path);
        paging = merge({
            start: 0,
            count: 25,
            sort: 'recent'
        }, paging);
        o = commentsQuery(this, resource, paging);
        ids = this.query({
            query: o.query,
            resultType: 'Comments'
        });
        length = ids.length;
        for (i = 0; i < length; i++) {
            comments.push(this.get(ids[i]));
        }
        //we have to manually sort this due to the bug in registry.getChildren() (#1 above)
        comments.sort(o.sorter);
        return o.paged ? comments : comments.slice(paging.start, paging.start + paging.count);
    };

    Registry.prototype.uncomment = function (path) {
        this.registry.removeComment(path);
    };

    Registry.prototype.rate = function (path, rating) {
        this.registry.rateResource(path, rating);
    };

    Registry.prototype.unrate = function (path) {
        this.registry.rateResource(path, 0);
    };

    Registry.prototype.rating = function (path, username) {
        var rating = {
            average: this.registry.getAverageRating(path)
        };
        if (username) {
            rating.user = this.registry.getRating(path, username);
        }
        return rating;
    };

    Registry.prototype.link = function (path, target) {
        return this.registry.createLink(path, target);
    };

    Registry.prototype.unlink = function (path) {
        return this.registry.removeLink(path);
    };

    Registry.prototype.search = function (query, paging) {
        var res = this.registry.searchContent(query);
        paging = merge({
            start: 0,
            count: 10,
            sort: 'recent'
        }, paging);
        return res ? content(this, res, paging) : [];
    };

    Registry.prototype.query = function (options) {
        var res,
            query = options.query,
            uuid = require('uuid'),
            name = options.name || new uuid.UUID(),
            cache = options.cache || true,
            Collections = java.util.Collections,
            path = '/_system/config/repository/components/org.wso2.carbon.registry/queries/' + name;
        if (!this.exists(path) || !cache) {
            this.put(path, {
                content: query,
                mediaType: 'application/vnd.sql.query',
                properties: {
                    resultType: options.resultType
                }
            });
        }
        res = this.registry.executeQuery(path, Collections.emptyMap());
        if (!cache) {
            this.remove(path);
        }
        return res.getChildren();
    };

}(server, registry));