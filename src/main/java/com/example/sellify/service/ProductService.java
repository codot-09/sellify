package com.example.sellify.service;

import com.example.sellify.dto.ProductRequest;
import com.example.sellify.dto.response.ProductDisplayResponse;
import com.example.sellify.dto.response.ProductResponse;
import com.example.sellify.entity.Photo;
import com.example.sellify.entity.Product;
import com.example.sellify.entity.User;
import com.example.sellify.entity.enums.Category;
import com.example.sellify.repository.ProductRepository;
import com.example.sellify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public void save(String chatId, ProductRequest request, String username) {
        User owner = userRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Owner not found"));

        List<Photo> photos = new ArrayList<>();
        request.getPhotoInfos().keySet().forEach(fileId -> {
            Photo photo = Photo.builder().fileId(fileId).build();
            photos.add(photo);
        });

        Product product = Product.builder()
                .owner(owner)
                .name(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(Category.valueOf(request.getCategory()))
                .active(request.isActive())
                .photos(photos)
                .build();

        productRepository.save(product);
    }

    public List<Product> getPendingProducts() {
        return productRepository.findByActiveFalse();
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public void delete(Long id){
        productRepository.deleteById(id);
    }

    @Transactional
    public void savePost(Product product) {
        product.setActive(true);
        productRepository.save(product);
    }

    public long count(){
        return productRepository.count();
    }

    public List<ProductDisplayResponse> getProducts(Category category){
        List<Product> products = productRepository.findRandom20ByCategory(category);
        return products.stream().map(product -> ProductDisplayResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .build()).toList();
    }

    public ProductResponse getProduct(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ProductResponse.builder()
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .photoUrls(product.getPhotos().stream().map(Photo::getFileUrl).toList())
                .ownerUsername(product.getOwner().getUsername())
                .build();
    }
}
