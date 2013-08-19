(function (server, registry) {

    var log = new Log();

    var GenericArtifactManager = Packages.org.wso2.carbon.governance.api.generic.GenericArtifactManager;
    var GenericArtifactFilter = Packages.org.wso2.carbon.governance.api.generic.GenericArtifactFilter;
    var ByteArrayInputStream = Packages.java.io.ByteArrayInputStream;
    var QName = Packages.javax.xml.namespace.QName;
    var IOUtils = Packages.org.apache.commons.io.IOUtils;
	
    var GovernanceUtils=Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;//Used to obtain Asset Types
    var DEFAULT_MEDIA_TYPE='application/vnd.wso2.registry-ext-type+xml';//Used to obtain Asset types

    var buildArtifact = function (manager, artifact) {
        return {
            id: String(artifact.id),
            type: String(manager.type),
            path: "/_system/governance" + String(artifact.getPath()),
            lifecycle: artifact.getLifecycleName(),
            lifecycleState: artifact.getLifecycleState(),
            mediaType: String(artifact.getMediaType()),
            attributes: (function () {
                var i, name,
                    names = artifact.getAttributeKeys(),
                    length = names.length,
                    attributes = {};
                for (i = 0; i < length; i++) {
                    name = names[i];
                    attributes[name] = String(artifact.getAttribute(name));
                }
                return attributes;
            }()),
            content: function () {
                return new Stream(new ByteArrayInputStream(artifact.getContent()));
            }
        };
    };

    var createArtifact = function (manager, options) {
        var name, attribute, i, length, lc,
            artifact = manager.newGovernanceArtifact(new QName(options.name)),
            attributes = options.attributes;
        for (name in attributes) {
            if (attributes.hasOwnProperty(name)) {
                attribute = attributes[name];
                if (attribute instanceof Array) {
                    /*length = attribute.length;
                     for (i = 0; i < length; i++) {
                     artifact.addAttribute(name, attribute[i]);
                     }*/
                    artifact.setAttributes(name, attribute);
                } else {
                    artifact.setAttribute(name, attribute);
                }
            }
        }
        if (options.id) {
            artifact.id = options.id;
        }
        if (options.content) {
            if (options.content instanceof Stream) {
                artifact.setContent(IOUtils.toByteArray(options.content.getStream()));
            } else {
                artifact.setContent(new java.lang.String(options.content).getBytes());
            }
        }
        lc = options.lifecycles;
        if (lc) {
            length = lc.length;
            for (i = 0; i < length; i++) {
                artifact.attachLifeCycle(lc[i]);
            }
        }
        return artifact;
    };

    var ArtifactManager = function (registry, type) {
        this.registry = registry;
        this.manager = new GenericArtifactManager(registry.registry.getChrootedRegistry("/_system/governance"), type);
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

    /*
	The function returns an array of asset types 
	@mediaType - The media type of the assets
	@return An array of strings containing the asset paths
    */
    ArtifactManager.prototype.getAssetTypePaths=function(mediaType){
	
	//Use the default media type if one is not provided
	if(!mediaType){
	   mediaType=DEFAULT_MEDIA_TYPE;
	}

	//var assetArray=GovernanceUtils.findGovernanceArtifacts(mediaType,this.registry);
	var result=Packages.org.wso2.carbon.governance.api.util.GovernanceUtils.findGovernanceArtifacts(mediaType,registry.registry);
	
	return result;
	//Create an empty array if no asset types are found
	//return (!assetArray)?[]:assetArray;
    };

    /*
     {
     name: 'AndroidApp1',
     attributes: {
     overview_status: "CREATED",
     overview_name: 'AndroidApp1',
     overview_version: '1.0.0',
     overview_url: 'http://overview.com',
     overview_provider: 'admin',
     images_thumbnail: 'http://localhost:9763/portal/gadgets/co2-emission/thumbnail.jpg',
     images_banner: 'http://localhost:9763/portal/gadgets/electric-power/banner.jpg'
     },
     lifecycles : ['lc1', 'lc2'],
     content : '<?xml ....>'
     }
     */
    ArtifactManager.prototype.add = function (options) {
        this.manager.addGenericArtifact(createArtifact(this.manager, options));
    };

    ArtifactManager.prototype.update = function (options) {
        this.manager.updateGenericArtifact(createArtifact(this.manager, options));
    };

    ArtifactManager.prototype.remove = function (id) {
        this.manager.removeGenericArtifact(id);
    };

   /*
   Attaches the provided lifecycle name to the artifact
   @lifecycleName: The name of a valid lifecycle.The lifecycle should be visible to the 
	registry.
   @options: The artifact to which the life cycle must be attached.
   */
   ArtifactManager.prototype.attachLifecycle=function(lifecycleName,options){

	var artifact=this.getArtifactFromImage(options);

	artifact.attachLifecycle(lifecycleName);

	//this.manager.updateGenericArtifact(artifact);
   };

   /*
   Removes the attached lifecycle from the artifact
   @options: The artifact from which the life cycle must be removed
   */
   ArtifactManager.prototype.detachLifecycle=function(options){
	
	var artifact=this.getArtifactFromImage(options);

	artifact.detachLifecycle();
   };

   /*
   Promotes the artifact to the next stage in its life cycle
   @options: An artifact image (Not a real artifact)
   */
   ArtifactManager.prototype.promoteLifecycleState=function(state,options){
	var artifact=this.getArtifactFromImage(options);
	artifact.invokeAction(state);
   };

   /*
   Gets the current lifecycle state
   @options: An artifact image
   @returns: The life cycle state
   */
   ArtifactManager.prototype.getLifecycleState=function(options){
	var artifact=this.getArtifactFromImage(options);

	var state=artifact.getLifecycleState();
	return state;
	//return artifact.getLcState();
   };

   /*
   Helper function to create an artifact instance from a set of options (an image).
   */
   ArtifactManager.prototype.getArtifactFromImage=function(options){
	var artifact=createArtifact(this.manager,{
		id:options.id,
		attributes:options.attributes
	});

	artifact.setArtifactPath(options.path);

	return artifact;
   };

}(server, registry));
