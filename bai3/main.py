from database import get_db
from schemas import ShipmentUpdate , ShipmentResponse
from Services import update_shipment_service

from sqlalchemy.orm import Session
from fastapi import FastAPI , Depends

app = FastAPI()

@app.put("/shipments/{shipment_id}" , response_model= ShipmentResponse)
def update_shipment(shipment_id : int, shipment_update : ShipmentUpdate , db : Session = Depends(get_db)):
    return update_shipment_service(db, shipment_id, shipment_update)