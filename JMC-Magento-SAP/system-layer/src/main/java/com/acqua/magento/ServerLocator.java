/**
 * ServerLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.acqua.magento;

public class ServerLocator extends org.apache.axis.client.Service implements com.acqua.magento.Server {

    public ServerLocator() {
    }


    public ServerLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ServerLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for serverPort
    private java.lang.String serverPort_address = "https://staging.aralsports.com.ar:443/integration/ws/store.php";

    public java.lang.String getserverPortAddress() {
        return serverPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String serverPortWSDDServiceName = "serverPort";

    public java.lang.String getserverPortWSDDServiceName() {
        return serverPortWSDDServiceName;
    }

    public void setserverPortWSDDServiceName(java.lang.String name) {
        serverPortWSDDServiceName = name;
    }

    public com.acqua.magento.ServerPortType getserverPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(serverPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getserverPort(endpoint);
    }

    public com.acqua.magento.ServerPortType getserverPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.acqua.magento.ServerBindingStub _stub = new com.acqua.magento.ServerBindingStub(portAddress, this);
            _stub.setPortName(getserverPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setserverPortEndpointAddress(java.lang.String address) {
        serverPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.acqua.magento.ServerPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.acqua.magento.ServerBindingStub _stub = new com.acqua.magento.ServerBindingStub(new java.net.URL(serverPort_address), this);
                _stub.setPortName(getserverPortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("serverPort".equals(inputPortName)) {
            return getserverPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:server", "server");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:server", "serverPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
		if ("serverPort".equals(portName)) {
		            setserverPortEndpointAddress(address);
		        }
		        else 
		{ // Unknown Port Name
		            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
		        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
