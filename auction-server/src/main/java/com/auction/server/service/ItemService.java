package com.auction.server.service;

import com.auction.common.entity.Art;
import com.auction.common.entity.Electronics;
import com.auction.common.entity.Item;
import com.auction.common.entity.Vehicle;
import com.auction.common.message.ClientResponse;
import com.auction.common.message.CreateItemRequest;
import com.auction.common.message.GetItemsRequest;
import com.auction.server.repository.SerializableItemRepository;

import java.util.Map;

import static com.auction.common.enums.ItemType.ELECTRONICS;

public class ItemService {
    static SerializableItemRepository sir=new SerializableItemRepository();
    //CREAT
    public ClientResponse C(CreateItemRequest cir){
        Map attr=cir.getExtraAttributes();
        Item i=null;
        switch (cir.getItemType()){
            case ELECTRONICS:
                i=new Electronics(cir.getName(), cir.getDescription(), cir.getStartingPrice(), cir.getSellerId(), (String) attr.get("brand"),(String) attr.get("model"), Integer.parseInt((String) attr.get("warrantyMonths")));
                break;
            case ART:
                i=new Art(cir.getName(), cir.getDescription(), cir.getStartingPrice(), cir.getSellerId(), (String) attr.get("artist"), Integer.parseInt((String) attr.get("year")), (String) attr.get("medium"));
                break;
            case VEHICLE:
                i=new Vehicle(cir.getName(), cir.getDescription(), cir.getStartingPrice(), cir.getSellerId(), (String) attr.get("manufacturer"), Integer.parseInt((String) attr.get("yearOfManufacture")), Integer.parseInt((String) attr.get("mileage")));
                break;
            default:
                return new ClientResponse(false, "Something went wrong", null);
        }
        sir.save(i);
        return new ClientResponse(true, "Created item successfully", i);
    }
    //READ
    public ClientResponse R(GetItemsRequest gir){
        sir.findById(gir.getSellerId());
        return new ClientResponse(true, "", null);
    }
    //UPDATE
    public void U(){

    }
    //DELETE
    public void D(){

    }
}
