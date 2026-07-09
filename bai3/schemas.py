from pydantic import BaseModel , Field
from datetime import datetime
from typing import Optional

class ShipmentUpdate(BaseModel):
    receiver_name: str
    delivery_address: str

class ShipmentResponse(BaseModel):
    id : int
    tracking_code : str
    receiver_name : str
    delivery_address : str 