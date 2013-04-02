(function (server, registry) {

    var log = new Log();

    var GenericArtifactManager = Packages.org.wso2.carbon.governance.api.generic.GenericArtifactManager;
    var GenericArtifactFilter = Packages.org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
    var ByteArrayInputStream = Packages.java.io.ByteArrayInputStream;

    var buildArtifact = function (manager, artifact) {
        return {
            id: String(artifact.id),
            type: String(manager.type),
            path: String(artifact.getPath()),
            lifecycle: artifact.getLifecycleName(),
            lifecycleState: artifact.getLifecycleState(),
            mediaType: String(artifact.getMediaType()),
            attribute: function (name) {
                return String(artifact.getAttribute(name));
            },
            attributes: function () {
                var i, name,
                    names = artifact.getAttributeKeys(),
                    length = names.length,
                    attributes = {};
                for (i = 0; i < length; i++) {
                    name = names[i];
                    attributes[name] = String(artifact.getAttribute(name));
                }
                return attributes;
            },
            content: function () {
                return new Stream(new ByteArrayInputStream(artifact.getContent()));
            }
        };
    };

    var ArtifactManager = function (registry, type) {
        this.registry = registry;
        this.manager = new GenericArtifactManager(registry.registry, type);
        this.type = type;
    };
    registry.ArtifactManager = ArtifactManager;

    ArtifactManager.prototype.find = function (fn, paging) {
        var i, length, artifacts,
            artifactz = [];
        artifacts = this.manager.findGenericArtifacts(new GenericArtifactFilter({
            matches: function (artifact) {
                return fn(buildArtifact(this, artifact));
            }
        }));
        length = artifacts.length;
        for (i = 0; i < length; i++) {
            artifactz.push(buildArtifact(this, artifacts[i]));
        }
        return artifactz;
    };

    ArtifactManager.prototype.get = function (id) {
        return buildArtifact(this, this.manager.getGenericArtifact(id));
    };

    ArtifactManager.prototype.count = function () {
        return this.manager.getAllGenericArtifactIds().length;
    };

    ArtifactManager.prototype.list = function (paging) {
        var i,
            artifactz = [],
            artifacts = this.manager.getAllGenericArtifacts(),
            length = artifacts.length;
        for (i = 0; i < length; i++) {
            artifactz.push(buildArtifact(this, artifacts[i]));
        }
        return artifactz;
    };

}(server, registry));