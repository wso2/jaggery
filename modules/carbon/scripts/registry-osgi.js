var registry = registry || {};

(function (server, registry) {
    var log = new Log();

    var Resource = Packages.org.wso2.carbon.registry.core.Resource;

    var Collection = Packages.org.wso2.carbon.registry.core.Collection;

    var Comment = Packages.org.wso2.carbon.registry.core.Comment;

    var content = function (resource, paging) {
        paging = paging || { start: 0, count: 10 };
        if (resource instanceof Collection) {
            return resource.getChildren(paging.start, paging.count);
        }
        var stream = resource.getContentStream();
        if (stream) {
            return new Stream(stream);
        }
        return resource.content;
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

    var Registry = function (serv, auth) {
        var tenantId,
            registryService = server.osgiService('org.wso2.carbon.registry.core.service.RegistryService');
        if (auth.username) {
            tenantId = server.tenantId({
                domain: auth.domain,
                username: auth.username
            });
            this.registry = registryService.getRegistry(auth.username, auth.password, tenantId);
            this.username = auth.username;
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
            res.content = resource.content;
            res.mediaType = resource.mediaType;
        }
        res.name = resource.name;
        res.description = resource.description;
        res.UUID = resource.uuid;

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
        var resource = this.registry.get(path);
        return {
            name: resource.name || resolveName(path),
            description: resource.description,
            created: {
                author: resource.authorUserName,
                time: resource.createdTime.time
            },
            updated: {
                author: resource.lastUpdaterUserName,
                time: resource.lastModified.time
            },
            content: content(resource),
            collection: (resource instanceof Collection),
            mediaType: resource.mediaType,
            uuid: resource.UUID,
            path: resource.path,
            properties: properties(resource),
            aspects: aspects(resource)
        };
    };

    Registry.prototype.exists = function (path) {
        return this.registry.resourceExists(path);
    };

    Registry.prototype.content = function (path, paging) {
        var resource = this.registry.get(path);
        return content(resource, paging);
    };

    Registry.prototype.tags = function (path) {
        var tag, tags, i, length, count, tz,
            tagz = [];
        if (path) {
            tags = this.registry.getTags(path);
            length = tags.length;
            for (i = 0; i < length; i++) {
                tag = tags[i];
                tagz.push({
                    name: tag.tagName,
                    count: tag.tagCount
                });
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
                    name: tag,
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
                type: asso.associationType,
                src: asso.sourcePath,
                dest: asso.destinationPath
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

    Registry.prototype.comments = function (path, paging) {
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
        return this.registry.getRating(path, username || this.username);
    };

    Registry.prototype.avgRating = function (path) {
        return this.registry.getAverageRating(path);
    };

    Registry.prototype.link = function (path, target) {
        return this.registry.createLink(path, target);
    };

    Registry.prototype.unlink = function (path) {
        return this.registry.removeLink(path);
    };

    Registry.prototype.search = function (query, paging) {
        var res = this.registry.searchContent(query);
        return res ? content(res, paging) : [];
    };

    Registry.prototype.query = function (options, paging) {
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
        res = content(this.registry.executeQuery(path, Collections.emptyMap()), paging);
        if (!cache) {
            this.remove(path);
        }
        return res;
    };

}(server, registry));