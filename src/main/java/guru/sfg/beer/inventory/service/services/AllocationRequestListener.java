package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.config.JmsConfig;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.AllocateOrderRequest;
import guru.sfg.brewery.model.events.AllocateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import static guru.sfg.beer.inventory.service.config.JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllocationRequestListener {
    private final AllocationService allocationService;
    private final JmsTemplate jmsTemplate;

    @Transactional
    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateOrderRequest event) {
        BeerOrderDto beerOrderDto = event.getBeerOrderDto();
        Boolean fullyAllocated = allocationService.allocateOrder(beerOrderDto);

        log.debug("Order " + beerOrderDto.getId() + (fullyAllocated ? "is" : "is not") + " fully allocated.");

        // TODO: AllocationError currently hardcoded to false.  This isn't something we generate right now.
        AllocateOrderResponse allocateOrderResponse = new AllocateOrderResponse(beerOrderDto,
                                                                                false,
                                                                                fullyAllocated);
        jmsTemplate.convertAndSend(ALLOCATE_ORDER_RESPONSE_QUEUE, allocateOrderResponse);
    }
}
