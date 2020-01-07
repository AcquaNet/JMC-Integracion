/**
 * ServerPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.acqua.magento;

public interface ServerPortType extends java.rmi.Remote {

    /**
     * Gestion de articulos
     */
    public java.lang.String gestionarStock(com.acqua.magento.Articulo[] articulos) throws java.rmi.RemoteException;
}
