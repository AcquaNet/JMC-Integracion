package sapb1masterpoll;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.mule.DefaultMuleEvent;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.construct.Flow;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.api.context.MuleContextAware;

public class OrdersFillAndSplit extends AbstractMessageTransformer implements MuleContextAware {
	private static final Logger LOG = Logger.getLogger("jmc_java.log");

	MuleContext muleContext;

	@Override
	public void setMuleContext(MuleContext context) {
		muleContext = context;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		// Define DB Login Information from FlowVars
		String user = message.getInvocationProperty("DBUser");
		String password = message.getInvocationProperty("DBPass");
		String connectionString = message.getInvocationProperty("DBConnection");

		// String codigo = message.getInvocationProperty("codigo");
		String which = message.getInvocationProperty("Fetch_requestPath");
		HashMap<String, Object> destinationMap = message.getInvocationProperty("Fetch_OrderDestinations");
		HashMap<String, String> destinationGestion = message.getInvocationProperty("Fetch_OrdersGestion");
		String destinations = message.getInvocationProperty("Fetch_ShortDestinations");
		String dbPadre = message.getInvocationProperty("DBPadre");
		String[] fulldestinations = ((String) message.getInvocationProperty("Fetch_destinations")).split(",");

		if (!which.equals("Orders")) {
			return message.getPayload();
		}
		LOG.info("User:" + user + " - Pass: " + password + " - Connect: " + connectionString);
		// Create a connection manager with all the info
		ODBCManager manager = new ODBCManager(user, password, connectionString);

		Connection connect = manager.connect();
		// Create a response layout
		HashMap<String, Object> response = new HashMap<String, Object>();

		if (connect == null) {
			response.put("mensaje", "Fallo la conexion a DB");
			return response;

		}
		System.out.println("Connection to HANA successful!");
		try {

			// ArrayList de todos los documentos ya divididos.
			ArrayList<HashMap<String, Object>> returnDocuments = new ArrayList<>();
			// ArrayList de todos los documentos para actualizar en padre
			ArrayList<HashMap<String, Object>> updateDocuments = new ArrayList<>();

			// Defino globalmente entre todos los documentos el stock de las hijas, para
			// evitar sobrelapar stock
			HashMap<String, ArrayList<HashMap<String, Object>>> ItemStock = new HashMap<>();

			// Loopeo por cada orden en el payload
			for (HashMap<String, Object> map : (ArrayList<HashMap<String, Object>>) message.getPayload()) {
				if (map.get("DocumentStatus").equals("bost_Close")) {
					continue;
				}
				Boolean emailIssue = false;
				String emailAnswerBody = "";
				// Lineas del documento
				ArrayList<HashMap<String, Object>> documentLines = (ArrayList<HashMap<String, Object>>) map
						.get("DocumentLines");
				LOG.info("Processing Order DocNum " + map.get("DocNum"));
				// Checkeo si el documento ya tiene asignado todo el stock
				Boolean isComplete = true;
				for (HashMap<String, Object> documentLine : documentLines) {
					if (documentLine.get("WarehouseCode").equals("01")) {
						isComplete = false;
						break;
					}

				}

				String[] soc = destinations.split(",");
				HashMap<String, Object> SocietyLines = new HashMap<>();

				// Caso que no esta completo y hay que asignar ubicaciones
				if (!isComplete) {
					LOG.info("Order is not complete");
					emailAnswerBody = "";

					ArrayList<HashMap<String, Object>> linesToSendFromDocument = new ArrayList<>();
					for (HashMap<String, Object> documentLine : documentLines) {
						// Si la linea ya tiene asignado stock, la ignoramos
						if (!documentLine.get("WarehouseCode").equals("01")) {
							linesToSendFromDocument.add(documentLine);
							LOG.info("Line without WH 01");
						}
						// Si la linea no tiene stock, le intentammos asignar
						else {
							String codigo = (String) documentLine.get("ItemCode");
							Double quantity = (Double) documentLine.get("Quantity");
							ArrayList<HashMap<String, Object>> stockLines = null;
							ArrayList<HashMap<String, Object>> updatedStockLines = new ArrayList<>();
							if (ItemStock.get(codigo) != null) {
								stockLines = ItemStock.get(codigo);
							} else {
								stockLines = fetchStockForSocieties(soc, manager, codigo, dbPadre);
								ItemStock.put(codigo, stockLines);
							}
							if (stockLines != null) {

								for (HashMap<String, Object> stockLine : stockLines) {
									// Loopeo por todas las ubicaciones de stock para ese articulo
									if ((Double) stockLine.get("Quantity") > 0) {
										// Si todavia tiene stock esa ubicación
										HashMap<String, Object> lineClone = (HashMap<String, Object>) documentLine
												.clone();
										lineClone.remove("LineTotal");
										lineClone.remove("DiscountPercent");
										// lineClone.remove("GrossProfitTotalBasePrice");
										// lineClone.remove("GrossTotal");
										// lineClone.remove("GrossTotalFC");
										// lineClone.remove("GrossTotalSC");
										// lineClone.remove("TaxTotal");
										// lineClone.remove("RowTotalSC");
										// lineClone.remove("RowTotalFC");
										// lineClone.remove("OpenAmountSC");
										// lineClone.remove("OpenAmountFC");
										// lineClone.remove("OpenAmount");
										// lineClone.remove("NetTaxAmountSC");
										// lineClone.remove("NetTaxAmountFC");
										// lineClone.remove("NetTaxAmount");

										if ((Double) stockLine.get("Quantity") < quantity) {
											// Si la ubicacion tiene stock insuficiente, le restamos al requerido y la
											// sacamos del stock

											lineClone.put("Quantity", stockLine.get("Quantity"));
											lineClone.put("WarehouseCode", stockLine.get("WhsCode"));
											linesToSendFromDocument.add(lineClone);
											quantity = quantity - (double) stockLine.get("Quantity");
											stockLine.put("Quantity", 0.0);
											// updatedStockLines.add(stockLine);
										} else if ((Double) stockLine.get("Quantity") == quantity) {
											// Si tiene la cantidad exacta, le restamos al requerido y la sacamos del
											// stock.
											lineClone.put("Quantity", stockLine.get("Quantity"));
											lineClone.put("WarehouseCode", stockLine.get("WhsCode"));
											linesToSendFromDocument.add(lineClone);
											quantity = 0.0;
											stockLine.put("Quantity", 0.0);
											// updatedStockLines.add(stockLine);
											break;
										} else {
											// Si tiene mas que la necesaria, le restamos lo requerido y la devolvemes
											// al
											// stock.
											lineClone.put("Quantity", quantity);
											lineClone.put("WarehouseCode", stockLine.get("WhsCode"));
											linesToSendFromDocument.add(lineClone);

											stockLine.put("Quantity", (Double) stockLine.get("Quantity") - quantity);
											quantity = 0.0;
											updatedStockLines.add(stockLine);
											break;
										}
									}
								}
							} else {
								LOG.info("Stock not found for item " + codigo);
							}
							if (quantity > 0) {
								// No alcanzó el stock en las hijas para satisfacer la necesidad de la linea de
								// la OV.
								// Agrego la linea con almacen 01 con la cantidad faltante
								HashMap<String, Object> lineClone = (HashMap<String, Object>) documentLine.clone();
								lineClone.put("Quantity", quantity);
								lineClone.remove("LineTotal");
								lineClone.remove("DiscountPercent");
								// lineClone.remove("GrossProfitTotalBasePrice");
								// lineClone.remove("GrossTotal");
								// lineClone.remove("GrossTotalFC");
								// lineClone.remove("GrossTotalSC");
								// lineClone.remove("TaxTotal");
								// lineClone.remove("RowTotalSC");
								// lineClone.remove("RowTotalFC");
								// lineClone.remove("OpenAmountSC");
								// lineClone.remove("OpenAmountFC");
								// lineClone.remove("OpenAmount");
								// lineClone.remove("NetTaxAmountSC");
								// lineClone.remove("NetTaxAmountFC");
								// lineClone.remove("NetTaxAmount");
								linesToSendFromDocument.add(lineClone);
								int value = (int) Math.round(quantity);
								emailAnswerBody = emailAnswerBody + "	<tr>\r\n" + "		<th>'" + codigo
										+ "'</th>\r\n" + "		<th>" + value + "</th>\r\n" + "	</tr>";
								emailIssue = true;
							}

							// Devolvemos al stock global la linea actualizada
							ItemStock.put(codigo, updatedStockLines);
						}
					}
					// Ya terminamos de loopear por todas las lineas originales, ahora armamos el
					// documento para actualizar
					HashMap<String, Object> newDocument = (HashMap<String, Object>) map.clone();
					int i = 0;
					ArrayList<HashMap<String, Object>> orderedLines = new ArrayList<>();

					for (HashMap<String, Object> documentLine : linesToSendFromDocument) {
						documentLine.put("LineNum", i);
						i++;
						orderedLines.add(documentLine);
					}
					// Pongo las lineas
					newDocument.put("DocumentLines", orderedLines);
					// Agrego el documento a la lista para enviar
					if (emailIssue) {
						newDocument.put("EmailIssue", true);
						newDocument.put("EmailBody", emailAnswerBody);
					}
					updateDocuments.add(newDocument);
				}
				// Caso que el documento esta completo
				else {
					LOG.info("Order is completly assigned. " + documentLines.size() + " lines");

					Boolean allAssigned = true;
					String failedinfo = null;
					String originalLine = null;
					int a = 1;
					for (HashMap<String, Object> documentLine : documentLines) {
						LOG.info("Looping the " + a + " line. ItemCode " + documentLine.get("ItemCode"));
						String whs = (String) documentLine.get("WarehouseCode");
						Pattern pat = Pattern.compile("(\\w+)_(\\d+)");
						Matcher match = pat.matcher(whs);
						String whscode = null;
						String whsdestination = null;

						if (match.matches()) {
							whscode = match.group(1);
							whsdestination = match.group(2);
						}
						if (whscode == null || whsdestination == null) {
							allAssigned = false;
							failedinfo = "Almacen intento: " + whs + "\nAlmacen: " + whscode + "\nDestino: "
									+ whsdestination;
							LOG.info("Failed WhsCode and WhsDestination: " + failedinfo);
							originalLine = JSONUtil.javaToJSONToString(documentLine);
							break;
						} else {
							ArrayList<HashMap<String, Object>> Lines = null;
							if (SocietyLines.get(whscode) != null) {
								LOG.info("SocietyLines " + whscode + " is not null");
								Lines = (ArrayList<HashMap<String, Object>>) SocietyLines.get(whscode);
							} else {
								LOG.info("SocietyLines " + whscode + " is null");
								Lines = new ArrayList<HashMap<String, Object>>();
							}
							LOG.info("Lines total currently in " + whscode + ": " + Lines.size());
							documentLine.put("WarehouseCode", whsdestination);
							Lines.add(documentLine);

							String destination = (String) destinationMap.get(whscode);
							if (destination != null) {
								SocietyLines.put(whscode, Lines);
								LOG.info("Saved Lines into SocietyLines. " + whscode + " TotalLines: " + Lines.size());
							} else {
								allAssigned = false;
								failedinfo = "Almacen intento: " + whs + "\nAlmacen: " + whscode + "\nDestino: "
										+ whsdestination + "\nDestinationMapFind: " + destination + "\nDestinationMap: "
										+ JSONUtil.javaToJSONToString(destinationMap);
								originalLine = JSONUtil.javaToJSONToString(documentLine);
								LOG.info("Failed destinationMatching: " + failedinfo);
								break;
							}
						}
						a++;
					}
					LOG.info("Ammount of destinations for this order: " + SocietyLines.keySet().size());

					if (allAssigned) {
						LOG.info("All Lines were assigned destinations properly");
						// Si el destino tiene lineas, crear documento
						for (String destination : soc) {
							ArrayList<HashMap<String, Object>> destLines = (ArrayList<HashMap<String, Object>>) SocietyLines
									.get(destination);

							if (destLines != null) {

								HashMap<String, Object> newDocument = (HashMap<String, Object>) map.clone();
								String fulldestination = (String) destinationMap.get(destination);
								newDocument.put("destination", fulldestination);
								LOG.info("Destination " + destination + " has " + destLines.size() + " lines");
								ArrayList<HashMap<String, Object>> newLineArray = new ArrayList<>();

								if (newDocument.get("U_Torden").equals("PS")) {
									// Si es pedido Segmentado
									String destinoGestion = destinationGestion.get(fulldestination);
									HashMap<String, Object> newDocumentGestion = (HashMap<String, Object>) newDocument
											.clone();
									newDocumentGestion.put("destination", destinoGestion);
									Double docT = (double) newDocumentGestion.get("DocTotal");
									Double vatS = (double) newDocumentGestion.get("VatSum");
									Double porcentaje = Double.valueOf((String) newDocumentGestion.get("U_porcentaje"));

									BigDecimal porcentajeTest = new BigDecimal(porcentaje, MathContext.DECIMAL64);

									// BigDecimal originalTotal = new BigDecimal(docT, MathContext.DECIMAL64);
									// BigDecimal originalVatSum = new BigDecimal(vatS, MathContext.DECIMAL64);

									// Sum of Lines
									LOG.info("Procesando Suma de Lineas de DocNum " + newDocumentGestion.get("DocNum")
											+ " para " + destinoGestion);
									BigDecimal X = destLines.stream().map(k -> {
										//
										LOG.info("Line Price: " + k.get("UnitPrice"));
										LOG.info("Line Quantity: " + k.get("Quantity"));
										BigDecimal lineTotal = new BigDecimal((double) k.get("UnitPrice"),
												MathContext.DECIMAL64);
										lineTotal = lineTotal.multiply(
												new BigDecimal((double) k.get("Quantity"), MathContext.DECIMAL64));
										LOG.info("Line pre-discount total: " + lineTotal);
										BigDecimal descuento = new BigDecimal((double) porcentaje,
												MathContext.DECIMAL64);
										LOG.info("Descuento Porcentaje cabecera: " + descuento);
										BigDecimal descuentoMultiplicativo = new BigDecimal(100, MathContext.DECIMAL64)
												.subtract(descuento).divide(new BigDecimal(100, MathContext.DECIMAL64),
														4, RoundingMode.HALF_UP);
										LOG.info("Descuento multiplicativo: " + descuentoMultiplicativo);
										// LOG.info("Discount percent: "+descuento );
										lineTotal = lineTotal.multiply(descuentoMultiplicativo);
										LOG.info("Line " + k.get("LineNum") + " total: " + lineTotal);
										return lineTotal;
									}).reduce(BigDecimal::add).get();

									double discountPercent = 0.0;

									if (newDocumentGestion.get("DiscountPercent") != null) {
										try {
											discountPercent = (double) newDocumentGestion.get("DiscountPercent");
										} catch (Exception e) {
											LOG.info("DiscountPercent is not a double");
										}
									}

									BigDecimal descuentoCabecera = new BigDecimal(discountPercent,
											MathContext.DECIMAL64);

									LOG.info("DiscountPercent Doc: " + descuentoCabecera);
									descuentoCabecera = new BigDecimal(100, MathContext.DECIMAL64)
											.subtract(descuentoCabecera);
									LOG.info("DiscountPercent Doc Inv: " + descuentoCabecera);
									descuentoCabecera = descuentoCabecera
											.divide(new BigDecimal(100, MathContext.DECIMAL64));
									LOG.info("DiscountPercent Doc Mult: " + descuentoCabecera);

									// LOG.info("Sum diff: " + (docT - vatS) + " - " + X);
									LOG.info("Descuento porcentaje: " + newDocumentGestion.get("DiscountPercent")
											+ " - " + descuentoCabecera);

									// LOG.info("DocNum: " + newDocumentGestion.get("DocNum"));

									X = X.multiply(descuentoCabecera);
									LOG.info("Sum diff: " + (docT - vatS) + " - " + X);
									// destLines.stream().forEach((k) -> {
									// X..add(new BigDecimal((double) k.get("UnitPrice"), MathContext.DECIMAL64));
									// });

									// X = 3840
									// X * 0.8 = 3456
									// M = 100-20 = 80
									// Y = x * 100 / M = 4320
									// Z = 4320 - 3456 =
									LOG.info("X: " + X);
									BigDecimal M = new BigDecimal(100, MathContext.DECIMAL64).subtract(porcentajeTest);
									LOG.info("M: " + M);

									BigDecimal Y = X.multiply(new BigDecimal(100, MathContext.DECIMAL64)).divide(M, 4,
											RoundingMode.HALF_UP);
									LOG.info("Y: " + Y);
									BigDecimal Z = Y.subtract(X);
									LOG.info("Z: " + Z);

									// System.out.println("X: "+X);
									// System.out.println("M: "+M);
									// System.out.println("Y: "+Y);
									// System.out.println("Z: "+Z);
									newDocumentGestion.remove("DocumentLines");
									newDocumentGestion.remove("DocTotal");
									newDocumentGestion.remove("VatSum");
									newDocumentGestion.remove("VatSumSys");
									HashMap<String, Object> lineaUnicaGestion = new HashMap<String, Object>();
									lineaUnicaGestion.put("ItemCode", "ventas");
									lineaUnicaGestion.put("LineNum", 1);
									lineaUnicaGestion.put("Quantity", 1);
									lineaUnicaGestion.put("WarehouseCode", "01");
									lineaUnicaGestion.put("Price", Z);
									newDocumentGestion.put("U_porcentaje", M);
									ArrayList<HashMap<String, Object>> docLines = new ArrayList<>();
									docLines.add(lineaUnicaGestion);
									newDocumentGestion.put("DocumentLines", docLines);
									returnDocuments.add(newDocumentGestion);
								}

								newDocument.remove("DocumentLines");
								newDocument.remove("DocTotal");
								newDocument.remove("VatSum");
								newDocument.remove("VatSumSys");

								int i = 0;
								for (HashMap<String, Object> lineMap : destLines) {
									lineMap.put("LineNum", i);
									lineMap.put("DiscountPercent", newDocument.get("U_porcentaje"));
									i++;
									newLineArray.add(lineMap);
								}
								newDocument.put("DocumentLines", newLineArray);
								returnDocuments.add(newDocument);
							}
						}
					} else {
						LOG.info("A Line failed to be assigned a destination properly");
						// Hubo un problema en asignar los almacenes
						Set<String> vars = message.getInvocationPropertyNames();
						HashMap<String, Object> flowVars = new HashMap<String, Object>();
						for (String str : vars) {
							flowVars.put(str, message.getInvocationProperty(str));
						}

						flowVars.put("ErrorCodeReason", "Fallo la asignación de almacenes en la OV");
						flowVars.put("ObjectID", "Num:" + map.get("DocNum") + " | Ent:" + map.get("DocEntry"));
						flowVars.put("flowVars.RESTType", "Orders (Pre-Destino)");
						flowVars.put("messageSaved", originalLine);
						message.setPayload(failedinfo);
						invokeMuleFlow(message, muleContext, "B1_Sync_EmailReportingFlow", flowVars);

					}
				}

			} // Fin documentos

			for (HashMap<String, Object> document : updateDocuments) {
				// Build up for invocation
				AtomicInteger counter = new AtomicInteger(0);
				List<Object> smollist = ((ArrayList<HashMap<String, Object>>) document.get("DocumentLines")).stream()
						.map(k -> k.get("WarehouseCode") + " " + k.get("ItemCode") + " " + k.get("Quantity"))
						.collect(Collectors.toList());

				((ArrayList<HashMap<String, Object>>) document.get("DocumentLines")).stream().forEach(linea -> {
					((ArrayList<HashMap<String, Object>>) linea.get("LineTaxJurisdictions")).forEach(lineaInterna -> {
						lineaInterna.put("LineNumber", counter.get());
					});
					counter.getAndIncrement();
				});
				String email = getEmailFromDocumentId((Integer) document.get("DocEntry"), manager, dbPadre);
				String flowName = "B1_Sync_ReportOVResult";

				// Remove info
				document.remove("EmailIssue");
				document.remove("EmailBody");

				Set<String> vars = message.getInvocationPropertyNames();
				HashMap<String, Object> flowVars = new HashMap<String, Object>();
				for (String str : vars) {
					flowVars.put(str, message.getInvocationProperty(str));
				}

				flowVars.put("OrderDocNum", document.get("DocEntry"));
				MuleEvent event = null;
				flowVars.put("Fetch_OrderSavedPayload", JSONUtil.javaToJSONToString(document));
				LOG.info("Documento a enviar: " + JSONUtil.javaToJSONToString(document));
				event = invokeMuleFlow(message, muleContext, "B1_Sync_SendUpdatedOrders", flowVars);

				if ((int) event.getMessage().getInboundProperty("http.status") > 299) {
					// System.out.println("Issue is BAD REQUEST, Message error: ");
					// System.out.println("URL: "+event.getFlowVariable("RESTType")+ " "+
					// event.getFlowVariable("requestURL"));
					// System.out.println(event.getMessage().getPayloadAsString());
					Set<String> ErrorVars = event.getFlowVariableNames();
					HashMap<String, Object> ErrorFlowVars = new HashMap<String, Object>();
					for (String str : ErrorVars) {
						ErrorFlowVars.put(str, event.getFlowVariable(str));
					}

					if (event.getMessage().getInboundProperty("http.status").equals(400)) {
						ErrorFlowVars.put("ErrorCodeReason", "Error de datos actualizando OV en padre");
					} else if (event.getMessage().getInboundProperty("http.status").equals(404)) {
						ErrorFlowVars.put("ErrorCodeReason", "Error de datos actualizando OV en padre 404");
					}
					ErrorFlowVars.put("ObjectID",
							"Num:" + document.get("DocNum") + " | Ent:" + document.get("DocEntry"));
					ErrorFlowVars.put("RESTType", "Orders (Origen)");
					ErrorFlowVars.put("messageSaved", flowVars.get("Fetch_OrderSavedPayload"));
					invokeMuleFlow(event.getMessage(), event.getMuleContext(), "B1_Sync_EmailReportingFlow",
							ErrorFlowVars);

				} // Fin del if
				
				if (!email.equals("no email")) {
					// Si Falto asignar stock
					if (document.containsKey("EmailIssue")) {
						String EmailBody = "<p>La orden de venta no se actualizó " + "correctamente en JMCGROUP,"
								+ " por favor revisar los siguientes artículos tengan la cantidad faltante:\r\n</p>"
								+ "<table style=\"width:50%;\">\r\n" + "	<tr>\r\n"
								+ "		<th>Numero de Articulo</th>\r\n" + "		<th>Cantidad</th>\r\n" + "	</tr>"
								+ (String) document.get("EmailBody") + "" + "</table>";
						String EmailSubject = "Resultado de Asignación de Almacenes de Orden de Venta "
								+ document.get("DocNum");
						String EmailTarget = email;
						HashMap<String, Object> EmailflowVars = new HashMap<String, Object>();
						Set<String> vars2 = message.getInvocationPropertyNames();
						for (String str : vars2) {
							EmailflowVars.put(str, message.getInvocationProperty(str));
						}
						EmailflowVars.put("EmailBody", EmailBody);
						EmailflowVars.put("EmailSubject", EmailSubject);
						EmailflowVars.put("EmailTarget", EmailTarget);
						invokeMuleFlow(message, muleContext, flowName, EmailflowVars);
					} else // Si no falto asignar stock
					{
						String EmailBody = "La orden de venta se creó correctamente en JMCGROUP";
						String EmailSubject = "Resultado de Asignación de Almacenes de Orden de Venta "
								+ document.get("DocNum");
						String EmailTarget = email;
						HashMap<String, Object> EmailflowVars = new HashMap<String, Object>();
						Set<String> vars2 = message.getInvocationPropertyNames();
						for (String str : vars2) {
							EmailflowVars.put(str, message.getInvocationProperty(str));
						}
						EmailflowVars.put("EmailBody", EmailBody);
						EmailflowVars.put("EmailSubject", EmailSubject);
						EmailflowVars.put("EmailTarget", EmailTarget);
						invokeMuleFlow(message, muleContext, flowName, EmailflowVars);
					}
				}

			} // Fin loop updateDocuments de actualizar
			
			// Documentos listos para sync, iniciar las ordenes para el cerrador
			
			HashMap<String,Object> ordenMatching = new HashMap<>();
			
			for (HashMap<String,Object> map : returnDocuments) {
				if (ordenMatching.containsKey(""+map.get("DocNum"))){
					HashMap<String,Object> order = (HashMap<String, Object>) ordenMatching.get(""+map.get("DocNum"));
					ArrayList<String> sociedades = (ArrayList<String>) order.get("sociedades");
					sociedades.add((String) map.get("destination"));
					order.put("sociedades", sociedades);
					ordenMatching.put(""+map.get("DocNum"), order);
				}
				else
				{
					HashMap<String,Object> order = new HashMap<>();
					order.put("order", map.get("DocNum"));
					order.put("completadas", new ArrayList<String>());
					ArrayList<String> sociedades = new ArrayList<>();
					sociedades.add((String) map.get("destination"));
					ordenMatching.put(""+map.get("DocNum"), order);
					
				}
			}
			
			
			// Loopeo los documentos originales para registrarlos
			for (String docNum : ordenMatching.keySet()) {
				HashMap<String,Object> orden = (HashMap<String, Object>) ordenMatching.get(docNum);
				HashMap<String, Object> flowVars = new HashMap<String, Object>();
				flowVars.put("payloadForOrden", orden);
				invokeMuleFlow(message, muleContext, "b1_sync_ov_iniciarOrden", flowVars);
			}
			
			return returnDocuments;

		} catch (Exception e) {
			e.printStackTrace();
			if (e.getClass().getName().contains("SQLException")) {
				response.put("mensaje", "Error de SQL. Revisar");
				return response;
			}
		}

		return null;
	}

	private String getEmailFromDocumentId(Integer entry, ODBCManager manager, String dbPadre) throws SQLException {
		ArrayList<HashMap<String, Object>> lines = new ArrayList<>();
		manager.createStatement();

		String query = "SELECT T0.\"DocEntry\", T0.\"OwnerCode\", T1.\"email\" FROM " + dbPadre + ".ORDR T0 JOIN "
				+ dbPadre + ".OHEM T1 ON T0.\"OwnerCode\" = T1.\"empID\" AND T0.\"DocEntry\" = '" + entry + "'";
		LOG.info("SQL Query: " + query);
		ResultSet set = manager.executeQuery(query);

		String email = "no email";
		while (set.next() != false) {
			email = set.getString("email");
		}

		return email;
	}

	public static ArrayList<HashMap<String, Object>> fetchStockForSocieties(String[] soc, ODBCManager manager,
			String codigo, String dbPadre) throws SQLException {
		ArrayList<HashMap<String, Object>> lines = new ArrayList<>();
		for (String str : soc) {
			manager.createStatement();

			String almacenes = "'" + str + "_*'";
			String query = "SELECT (\"OnHand\" + \"OnOrder\" - \"IsCommited\") AS \"Cantidad\",\"ItemCode\", \"WhsCode\" FROM "
					+ dbPadre + ".OITW WHERE \"ItemCode\" = '" + codigo
					+ "' AND (\"OnHand\" + \"OnOrder\" - \"IsCommited\") > 0 AND CONTAINS(\"WhsCode\", " + almacenes
					+ ")";
			LOG.info("SQL Query: " + query);
			ResultSet set = manager.executeQuery(query);

			// ResultSet set = manager.executeQuery("SELECT \"ItemCode\", \"WhsCode\",
			// \"OnHandQty\", \"BinAbs\" FROM "
			// + str + ".OIBQ WHERE \"OnHandQty\" > 0 AND \"Freezed\" = 'N' AND \"ItemCode\"
			// = '" + codigo + "'");
			while (set.next() != false) {
				HashMap<String, Object> map = new HashMap<>();
				map.put("Sociedad", str);
				map.put("WhsCode", set.getString("WhsCode"));
				map.put("ItemCode", set.getString("ItemCode"));
				map.put("Quantity", set.getDouble("Cantidad"));
				lines.add(map);
			}
		}

		return lines;
	}

	public static MuleEvent invokeMuleFlow(MuleMessage muleMessage, MuleContext muleContext, String flowName,
			HashMap<String, Object> flowVars) throws Exception {
		LOG.info("JAVA.WhileLoop.124: Flow LoopingFlow Lookup");
		Flow flow = (Flow) muleContext.getRegistry().lookupFlowConstruct(flowName);
		// System.out.println("Flow Found");
		LOG.info("JAVA.WhileLoop.127: Create muleEvent for invoking flow and set flowVars");
		MuleEvent muleEvent = new DefaultMuleEvent(muleMessage, MessageExchangePattern.REQUEST_RESPONSE, flow);
		for (String str : flowVars.keySet()) {
			muleEvent.setFlowVariable(str, flowVars.get(str));
		}
		LOG.info("JAVA.WhileLoop.132: Invoke flow");
		// System.out.println("Flow processing");
		return flow.process(muleEvent);
	}
}
