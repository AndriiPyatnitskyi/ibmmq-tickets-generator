package com.ibm.mq.events;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class RequestTickets {
    private int eventID;
    private int numberRequested;
}
