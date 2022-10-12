package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.config.JmsConfig;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import static guru.sfg.beer.inventory.service.config.JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class AllocationRequestListener {
    private final AllocationService allocationService;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateOrderRequest event) {
        BeerOrderDto beerOrderDto = event.getBeerOrderDto();

        Boolean fullyAllocated = false, allocationError = false;
        try {
            fullyAllocated = allocationService.allocateOrder(beerOrderDto);
            log.debug("Order " + beerOrderDto.getId() + (fullyAllocated ? "is" : "is not") + " fully allocated.");
        } catch (Exception ex) {
            log.error("Allocation failed for Order Id: " + beerOrderDto.getId(), ex);
            allocationError = true;
        }

        AllocateOrderResponse allocateOrderResponse = new AllocateOrderResponse(beerOrderDto,
                                                                                allocationError,
                                                                                !fullyAllocated);
        jmsTemplate.convertAndSend(ALLOCATE_ORDER_RESPONSE_QUEUE, allocateOrderResponse);
    }
}
