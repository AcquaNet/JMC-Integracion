# BusinessPartners:
```
{
    "error": {
        "code": -5002,
        "message": {
            "lang": "en-us",
            "value": "253000019 - Journal entries linked to card "
        }
    }
}
```
#### Causa: Diferente (Campo: "Currency") entre origen y destino.

-------------

# Items:

```
{
    "error": {
        "code": -5002,
        "message": {
            "lang": "en-us",
            "value": "Enter valid code  [ItemWarehouseInfoCollection.WarehouseCode] , 'A0096'"
        }
    }
}
sapb1masterpoll.StringToJSON.stringToMap(flowVars.messageSaved).ItemWarehouseInfoCollection
```
#### Causa: No existe un warehouse referenciado en origen en destino. (Campo: "ItemWarehouseInfoCollection")

```
{
    "error": {
        "code": -1029,
        "message": {
            "lang": "en-us",
            "value": "Field cannot be updated (ODBC -1029)"
        }
    }
}
```

#### Causa (Posible): Error con las referencias a warehouses. Campo de error: "ItemWarehouseInfoCollection"
#### Causa (Posible): WareHouseInfoCollection.StandardAveragePrice difiere.