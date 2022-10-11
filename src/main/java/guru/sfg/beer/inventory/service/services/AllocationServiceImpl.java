package guru.sfg.beer.inventory.service.services;

import guru.sfg.beer.inventory.service.domain.BeerInventory;
import guru.sfg.beer.inventory.service.repositories.BeerInventoryRepository;
import guru.sfg.brewery.model.BeerOrderDto;
import guru.sfg.brewery.model.BeerOrderLineDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class AllocationServiceImpl implements AllocationService {

    private final BeerInventoryRepository beerInventoryRepository;

    @Override
    public Boolean allocateOrder(BeerOrderDto beerOrderDto) {
        log.debug("Allocating OrderId: " + beerOrderDto.getId());

        AtomicInteger totalOrdered = new AtomicInteger();
        AtomicInteger totalAllocated = new AtomicInteger();

        beerOrderDto.getBeerOrderLines().forEach(beerOrderLine -> {
            if (get(beerOrderLine.getOrderQuantity()) - get(beerOrderLine.getQuantityAllocated()) > 0) {
                allocateBeerOrderLine(beerOrderLine);
            }
            totalOrdered.set(totalOrdered.get() + get(beerOrderLine.getOrderQuantity()));
            totalAllocated.set(totalAllocated.get() + get(beerOrderLine.getQuantityAllocated()));
        });

        log.debug("Total Ordered: " + totalOrdered.get() + " Total Allocated: " + totalAllocated.get());
        return totalOrdered.get() == totalAllocated.get();
    }

    @Override
    public BeerOrderDto deallocateOrder(BeerOrderDto beerOrderDto) {
        log.debug("Deallocating OrderId: " + beerOrderDto.getId());

        beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
            if (get(beerOrderLineDto.getQuantityAllocated()) > 0) {
                deallocateBeerOrderLine(beerOrderLineDto);
            }
        });

        return beerOrderDto;
    }

    private void allocateBeerOrderLine(BeerOrderLineDto beerOrderLineDto) {
        List<BeerInventory> beerInventoryList = beerInventoryRepository.findAllByUpc(beerOrderLineDto.getUpc());

        beerInventoryList.forEach(beerInventory -> {
            int inventory = get(beerInventory.getQuantityOnHand());
            int orderQty = get(beerOrderLineDto.getOrderQuantity());
            int allocatedQty = get(beerOrderLineDto.getQuantityAllocated());
            int qtyToAllocate = orderQty - allocatedQty;

            if (inventory >= qtyToAllocate) { // full allocation
                inventory = inventory - qtyToAllocate;
                beerOrderLineDto.setQuantityAllocated(orderQty);
                beerInventory.setQuantityOnHand(inventory);
                beerInventoryRepository.save(beerInventory);
            } else if (inventory > 0) { // partial allocation
                beerOrderLineDto.setQuantityAllocated(allocatedQty + inventory);
                beerInventory.setQuantityOnHand(0);
                beerInventoryRepository.delete(beerInventory);
            }
        });
    }

    private void deallocateBeerOrderLine(BeerOrderLineDto beerOrderLineDto) {
        BeerInventory beerInventory = BeerInventory.builder()
                .beerId(beerOrderLineDto.getBeerId())
                .quantityOnHand(beerOrderLineDto.getQuantityAllocated())
                .upc(beerOrderLineDto.getUpc())
                .version(Long.valueOf(beerOrderLineDto.getVersion())) // <- This?
                .build();
        beerOrderLineDto.setQuantityAllocated(0);
        beerInventoryRepository.save(beerInventory);
        log.debug("Saved inventory for beer upc: " + beerInventory.getUpc() + " inventory id: " + beerInventory.getId());
    }

    /**
     * Squash null to zero.
     *
     * @param qty
     * @return
     */
    private static final int get(Integer qty) {
        return qty != null ? qty : 0;
    }
}
