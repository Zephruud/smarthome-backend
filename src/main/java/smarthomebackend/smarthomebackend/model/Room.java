package smarthomebackend.smarthomebackend.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class Room {
    private String id;
    private String name;
    private List<Device> devices = new ArrayList<>();
}

