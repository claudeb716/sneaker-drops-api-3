package com.pluralsight.sneakerdrops.service;

import com.pluralsight.sneakerdrops.data.BrandRepository;
import com.pluralsight.sneakerdrops.data.SneakerRepository;
import com.pluralsight.sneakerdrops.models.Brand;
import com.pluralsight.sneakerdrops.models.Sneaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


// (A Bean that holds business logic)
@Service
public class SneakerService {

    private  final SneakerRepository sneakerRepository;
    private  final BrandRepository brandRepository;
    // Constructor Injection
@Autowired
    public SneakerService(SneakerRepository sneakerRepository, BrandRepository brandRepository) {
        this.sneakerRepository = sneakerRepository;
        this.brandRepository = brandRepository;
    }

    public Sneaker byId(long id){
    return sneakerRepository.findById(id).orElse(null);
    }

    public List<Sneaker> search(Integer year,String model,String brand, Double minPrice, Double maxPrice, String sort){
    List<Sneaker> results = new ArrayList<>(sneakerRepository.findAll().stream()
            .filter(sneaker -> year == null || sneaker.getReleaseYear() == year)
            .filter(sneaker -> model == null || sneaker.getModel().toLowerCase().contains(model.toLowerCase()))
            .filter(sneaker -> brand == null || sneaker.getBrand() != null && sneaker.getBrand().getName().equalsIgnoreCase(brand))
            .filter(sneaker -> minPrice == null || sneaker.getPrice() >= minPrice)
            .filter(sneaker -> maxPrice == null || sneaker.getPrice() <= maxPrice)
            .toList());
    //sort
    if ("price".equalsIgnoreCase(sort)){
        results.sort(Comparator.comparingDouble(Sneaker::getPrice));
    }else if ("models".equalsIgnoreCase(sort)){
        results.sort(Comparator.comparing(Sneaker::getModel));
    }
    return results;
    }

    public Sneaker createSneaker(Sneaker sneaker) {
    sneaker.setId(null); // creating id
        sneaker.setBrand(resolveBrand(sneaker)); //resolve the brand(user only provided the id)
    return sneakerRepository.save(sneaker); //create sneaker
}

    public Sneaker updateSneaker(long id, Sneaker updated){

        Sneaker existing = byId(id);
        if (existing == null) //search if id exist and if null return null to controller
            return  null;
        existing.setModel(updated.getModel());
        existing.setReleaseYear(updated.getReleaseYear());
        existing.setPrice(updated.getPrice());
        existing.setBrand(resolveBrand(updated));
        //already has id so it treats it as a update
        return sneakerRepository.save(updated);
    }

    public void deleteSneaker(long id){
    if (!sneakerRepository.existsById(id)){
        throw new NotFoundException("No Sneaker with ID " + id);
    }
    sneakerRepository.deleteById(id);
    }

    public void seedIfEmpty(){
        if (sneakerRepository.count() > 0) {
            return;
        }

        Brand nike = brandRepository.save(new Brand("Nike"));
        Brand jordan = brandRepository.save(new Brand("Jordan"));
        Brand adidas = brandRepository.save(new Brand("Adidas"));
        Brand newBalance = brandRepository.save(new Brand("New Balance"));
        Brand converse = brandRepository.save(new Brand("Converse"));


        sneakerRepository.save(new Sneaker("Nike Air Force 1",89.99,1982 , nike));
        sneakerRepository.save(new Sneaker("Air Jordan 1", 64.99, 1985, jordan));
        sneakerRepository.save(new Sneaker( "Samba", 100.00, 1950, adidas));
        sneakerRepository.save(new Sneaker("Men's 574", 74.99, 1988, newBalance ));
        sneakerRepository.save(new Sneaker(" Chuck Taylor All-Star", 65.00, 1917,converse));

    }
    //helper
    private Brand resolveBrand(Sneaker sneaker){
    if (sneaker.getBrand() == null || sneaker.getBrand().getId() == null) //if we dont have a brand or id or brand app will crash so return null
        return null;
    return brandRepository.findById(sneaker.getBrand().getId()).orElse(null);
    }

}
