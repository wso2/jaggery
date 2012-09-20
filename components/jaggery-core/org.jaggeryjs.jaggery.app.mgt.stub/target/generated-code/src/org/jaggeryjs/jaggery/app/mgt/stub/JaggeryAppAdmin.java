

/**
 * JaggeryAppAdmin.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.1-wso2v5  Built on : Aug 07, 2012 (01:20:15 PDT)
 */

    package org.jaggeryjs.jaggery.app.mgt.stub;

    /*
     *  JaggeryAppAdmin java interface
     */

    public interface JaggeryAppAdmin {
          

        /**
          * Auto generated method signature
          * 
                    * @param uploadWebapp0
                
         */

         
                     public boolean uploadWebapp(

                        org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappUploadData[] webappUploadDataList1)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param uploadWebapp0
            
          */
        public void startuploadWebapp(

            org.jaggeryjs.jaggery.app.mgt.stub.types.carbon.WebappUploadData[] webappUploadDataList1,

            final org.jaggeryjs.jaggery.app.mgt.stub.JaggeryAppAdminCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    