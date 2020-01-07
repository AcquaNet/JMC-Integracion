

package com.acqua.magento;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"DescMarca",
"DescCategoria2",
"FechaComprobante",
"NumeroComprobante",
"PorcDescuento",
"IdLinea2",
"Habilitado",
"EntradaSalida",
"CodArticulo",
"DescCategoria1",
"Precio",
"DescTemporada",
"IdGenero",
"IdTemporada",
"IdDisciplina1",
"DescLinea1",
"IdDisciplina2",
"DescLinea2",
"TipoComprobante",
"DescDisciplina2",
"DescDisciplina1",
"IdCategoria2",
"IdCategoria1",
"IdLinea1",
"Cantidad",
"DescColor",
"IdMarca",
"DesArticulo",
"DescGenero"
})
public class Articulo {

@JsonProperty("DescMarca")
private String descMarca;
@JsonProperty("DescCategoria2")
private String descCategoria2;
@JsonProperty("FechaComprobante")
private String fechaComprobante;
@JsonProperty("NumeroComprobante")
private Integer numeroComprobante;
@JsonProperty("PorcDescuento")
private Integer porcDescuento;
@JsonProperty("IdLinea2")
private String idLinea2;
@JsonProperty("Habilitado")
private String habilitado;
@JsonProperty("EntradaSalida")
private String entradaSalida;
@JsonProperty("CodArticulo")
private String codArticulo;
@JsonProperty("DescCategoria1")
private String descCategoria1;
@JsonProperty("Precio")
private Double precio;
@JsonProperty("DescTemporada")
private String descTemporada;
@JsonProperty("IdGenero")
private String idGenero;
@JsonProperty("IdTemporada")
private String idTemporada;
@JsonProperty("IdDisciplina1")
private String idDisciplina1;
@JsonProperty("DescLinea1")
private String descLinea1;
@JsonProperty("IdDisciplina2")
private Object idDisciplina2;
@JsonProperty("DescLinea2")
private String descLinea2;
@JsonProperty("TipoComprobante")
private String tipoComprobante;
@JsonProperty("DescDisciplina2")
private Object descDisciplina2;
@JsonProperty("DescDisciplina1")
private String descDisciplina1;
@JsonProperty("IdCategoria2")
private String idCategoria2;
@JsonProperty("IdCategoria1")
private String idCategoria1;
@JsonProperty("IdLinea1")
private String idLinea1;
@JsonProperty("Cantidad")
private Double cantidad;
@JsonProperty("DescColor")
private String descColor;
@JsonProperty("IdMarca")
private String idMarca;
@JsonProperty("DesArticulo")
private String desArticulo;
@JsonProperty("DescGenero")
private String descGenero;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("DescMarca")
public String getDescMarca() {
return descMarca;
}

@JsonProperty("DescMarca")
public void setDescMarca(String descMarca) {
this.descMarca = descMarca;
}

@JsonProperty("DescCategoria2")
public String getDescCategoria2() {
return descCategoria2;
}

@JsonProperty("DescCategoria2")
public void setDescCategoria2(String descCategoria2) {
this.descCategoria2 = descCategoria2;
}

@JsonProperty("FechaComprobante")
public String getFechaComprobante() {
return fechaComprobante;
}

@JsonProperty("FechaComprobante")
public void setFechaComprobante(String fechaComprobante) {
this.fechaComprobante = fechaComprobante;
}

@JsonProperty("NumeroComprobante")
public Integer getNumeroComprobante() {
return numeroComprobante;
}

@JsonProperty("NumeroComprobante")
public void setNumeroComprobante(Integer numeroComprobante) {
this.numeroComprobante = numeroComprobante;
}

@JsonProperty("PorcDescuento")
public Integer getPorcDescuento() {
return porcDescuento;
}

@JsonProperty("PorcDescuento")
public void setPorcDescuento(Integer porcDescuento) {
this.porcDescuento = porcDescuento;
}

@JsonProperty("IdLinea2")
public String getIdLinea2() {
return idLinea2;
}

@JsonProperty("IdLinea2")
public void setIdLinea2(String idLinea2) {
this.idLinea2 = idLinea2;
}

@JsonProperty("Habilitado")
public String getHabilitado() {
return habilitado;
}

@JsonProperty("Habilitado")
public void setHabilitado(String habilitado) {
this.habilitado = habilitado;
}

@JsonProperty("EntradaSalida")
public String getEntradaSalida() {
return entradaSalida;
}

@JsonProperty("EntradaSalida")
public void setEntradaSalida(String entradaSalida) {
this.entradaSalida = entradaSalida;
}

@JsonProperty("CodArticulo")
public String getCodArticulo() {
return codArticulo;
}

@JsonProperty("CodArticulo")
public void setCodArticulo(String codArticulo) {
this.codArticulo = codArticulo;
}

@JsonProperty("DescCategoria1")
public String getDescCategoria1() {
return descCategoria1;
}

@JsonProperty("DescCategoria1")
public void setDescCategoria1(String descCategoria1) {
this.descCategoria1 = descCategoria1;
}

@JsonProperty("Precio")
public Double getPrecio() {
return precio;
}

@JsonProperty("Precio")
public void setPrecio(Double precio) {
this.precio = precio;
}

@JsonProperty("DescTemporada")
public String getDescTemporada() {
return descTemporada;
}

@JsonProperty("DescTemporada")
public void setDescTemporada(String descTemporada) {
this.descTemporada = descTemporada;
}

@JsonProperty("IdGenero")
public String getIdGenero() {
return idGenero;
}

@JsonProperty("IdGenero")
public void setIdGenero(String idGenero) {
this.idGenero = idGenero;
}

@JsonProperty("IdTemporada")
public String getIdTemporada() {
return idTemporada;
}

@JsonProperty("IdTemporada")
public void setIdTemporada(String idTemporada) {
this.idTemporada = idTemporada;
}

@JsonProperty("IdDisciplina1")
public String getIdDisciplina1() {
return idDisciplina1;
}

@JsonProperty("IdDisciplina1")
public void setIdDisciplina1(String idDisciplina1) {
this.idDisciplina1 = idDisciplina1;
}

@JsonProperty("DescLinea1")
public String getDescLinea1() {
return descLinea1;
}

@JsonProperty("DescLinea1")
public void setDescLinea1(String descLinea1) {
this.descLinea1 = descLinea1;
}

@JsonProperty("IdDisciplina2")
public Object getIdDisciplina2() {
return idDisciplina2;
}

@JsonProperty("IdDisciplina2")
public void setIdDisciplina2(Object idDisciplina2) {
this.idDisciplina2 = idDisciplina2;
}

@JsonProperty("DescLinea2")
public String getDescLinea2() {
return descLinea2;
}

@JsonProperty("DescLinea2")
public void setDescLinea2(String descLinea2) {
this.descLinea2 = descLinea2;
}

@JsonProperty("TipoComprobante")
public String getTipoComprobante() {
return tipoComprobante;
}

@JsonProperty("TipoComprobante")
public void setTipoComprobante(String tipoComprobante) {
this.tipoComprobante = tipoComprobante;
}

@JsonProperty("DescDisciplina2")
public Object getDescDisciplina2() {
return descDisciplina2;
}

@JsonProperty("DescDisciplina2")
public void setDescDisciplina2(Object descDisciplina2) {
this.descDisciplina2 = descDisciplina2;
}

@JsonProperty("DescDisciplina1")
public String getDescDisciplina1() {
return descDisciplina1;
}

@JsonProperty("DescDisciplina1")
public void setDescDisciplina1(String descDisciplina1) {
this.descDisciplina1 = descDisciplina1;
}

@JsonProperty("IdCategoria2")
public String getIdCategoria2() {
return idCategoria2;
}

@JsonProperty("IdCategoria2")
public void setIdCategoria2(String idCategoria2) {
this.idCategoria2 = idCategoria2;
}

@JsonProperty("IdCategoria1")
public String getIdCategoria1() {
return idCategoria1;
}

@JsonProperty("IdCategoria1")
public void setIdCategoria1(String idCategoria1) {
this.idCategoria1 = idCategoria1;
}

@JsonProperty("IdLinea1")
public String getIdLinea1() {
return idLinea1;
}

@JsonProperty("IdLinea1")
public void setIdLinea1(String idLinea1) {
this.idLinea1 = idLinea1;
}

@JsonProperty("Cantidad")
public Double getCantidad() {
return cantidad;
}

@JsonProperty("Cantidad")
public void setCantidad(Double cantidad) {
this.cantidad = cantidad;
}

@JsonProperty("DescColor")
public String getDescColor() {
return descColor;
}

@JsonProperty("DescColor")
public void setDescColor(String descColor) {
this.descColor = descColor;
}

@JsonProperty("IdMarca")
public String getIdMarca() {
return idMarca;
}

@JsonProperty("IdMarca")
public void setIdMarca(String idMarca) {
this.idMarca = idMarca;
}

@JsonProperty("DesArticulo")
public String getDesArticulo() {
return desArticulo;
}

@JsonProperty("DesArticulo")
public void setDesArticulo(String desArticulo) {
this.desArticulo = desArticulo;
}

@JsonProperty("DescGenero")
public String getDescGenero() {
return descGenero;
}

@JsonProperty("DescGenero")
public void setDescGenero(String descGenero) {
this.descGenero = descGenero;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}