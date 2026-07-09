from models import ShipmentModel
from schemas import ShipmentUpdate

from sqlalchemy.orm import Session
from fastapi import HTTPException, status

def update_shipment_service(db : Session, shipment_id : int, shipment_update : ShipmentUpdate):
    shipment = db.query(ShipmentModel).filter(ShipmentModel.id == shipment_id).first()

    if shipment is None :
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail= "Shipment id is not existed"
        )
    
    shipment.receiver_name = shipment_update.receiver_name
    shipment.delivery_address = shipment_update.delivery_address

    db.commit()
    db.refresh(shipment)

    return shipment