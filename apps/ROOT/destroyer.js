
//destroy.js use keySet property to delete all indexed data when
//app is undeployed.

(function () {
    //server.deleteByQuery("*:*"); // delete everything
    server.deleteByQuery("*:*");
    server.commit();
})();
