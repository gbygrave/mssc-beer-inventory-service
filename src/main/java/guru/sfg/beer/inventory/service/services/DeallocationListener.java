package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.config.JmsConfig;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.events.DeallocateOrderRequest;
import guru.sfg.brewery.model.events.DeallocateOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeallocationListener {
    private final BeerInventoryRepository beerInventoryRepository;
    private final AllocationService       allocationService;
    private final JmsTemplate             jmsTemplate;

    @Transactional
    @JmsListener(destination = JmsConfig.DEALLOCATE_ORDER_QUEUE)
    public void listen(DeallocateOrderRequest event) {
        BeerOrderDto deallocatedBeerOrderDto = allocationService.deallocateOrder(event.getBeerOrderDto());
        jmsTemplate.convertAndSend(JmsConfig.DEALLOCATE_ORDER_RESPONSE_QUEUE,
                                   new DeallocateOrderResponse(deallocatedBeerOrderDto));

    }
}
