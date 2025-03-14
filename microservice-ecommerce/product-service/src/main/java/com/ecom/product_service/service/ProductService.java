package com.ecom.product_service.service;

import com.ecom.product_service.bean.ApiResponse;
import com.ecom.product_service.bean.ProductBean;
import com.ecom.product_service.bean.ProductImageBean;
import com.ecom.product_service.controller.BaseController;
import com.ecom.product_service.exeption.BaseException;
import com.ecom.product_service.exeption.ProductException;
import com.ecom.product_service.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ProductService extends BaseController {
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    public ProductService(ProductRepository productRepository, FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.fileStorageService = fileStorageService;
    }

    public ApiResponse getAllProduct(
            String search,
            int page_number,
            int page_size,
            String sort,
            String sort_type
    ) throws BaseException {
        ApiResponse res = new ApiResponse();
        HashMap<String, Object> params = new HashMap<>();
        try {
            if(search != null && !search.isEmpty()){
                params.put("search", search);
            }
            this.pagination(page_number, page_size, sort, sort_type, params);
            List<ProductBean> products = productRepository.findALlProduct(params);
            int productsCount = productRepository.findCountProduct();
            res.getPaginate().setLimit(page_size);
            res.getPaginate().setPage(page_number);
            res.getPaginate().setTotal(productsCount);

            res.setData(products);
        } catch (Exception e) {
            this.checkException(e, res);
        }
        return res;
    }

    public ApiResponse getProductById(Long id) throws BaseException {
        ApiResponse res = new ApiResponse();
        try {
            ProductBean product =  productRepository.findProductById(id);
            if(product == null){
                throw new ProductException("not.found", "product not found");
            }
            res.setData(product);
        } catch (Exception e) {
            this.checkException(e, res);
        }
        return res;
    }

    public ApiResponse createProduct(ProductBean productBean, List<MultipartFile> files) throws BaseException {
        ApiResponse res = new ApiResponse();
        try {
            if (files == null || files.isEmpty()) {
                throw new ProductException("image.at-least", "image at least 1");
            }

            productRepository.createProduct(productBean);

            List<ProductImageBean> productImages = new ArrayList<>();
            for (MultipartFile file : files) {
                String originFileName = file.getOriginalFilename();
                if (originFileName != null && !originFileName.isEmpty()) {
                    String fileExtension = originFileName.substring(originFileName.lastIndexOf("."));

                    String uniqueFileName = UUID.randomUUID().toString()
                            + "_"
                            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                            + fileExtension;

                    fileStorageService.saveProductImage(file, productBean.getId(), uniqueFileName);

                    ProductImageBean productImageBean = new ProductImageBean();
                    productImageBean.setProduct_id(productBean.getId());
                    productImageBean.setImage_url("/products/" + productBean.getId() + "/" + uniqueFileName);

                    productImages.add(productImageBean);
                }
            }

            productRepository.createProductImage(productImages);

        } catch (Exception e) {
            this.checkException(e, res);
        }
        return res;
    }

    public ApiResponse updateProduct(Long id, ProductBean productBean, List<MultipartFile> files) throws BaseException {
        ApiResponse res = new ApiResponse();
        try {

            if ((files == null || files.isEmpty()) && productBean.getProduct_image().isEmpty()) {
                throw new ProductException("image.at-least", "image at least 1");
            }

            productBean.setId(id);

            List<ProductImageBean> originalProductImage = productRepository.findProductImageByProductId(id);
            List<ProductImageBean> newProductImage = productBean.getProduct_image();
            Map<Long, ProductImageBean> newProductImageMap = new HashMap<>();
            List<Long> toDeleteProductImage = new ArrayList<>();

            for (ProductImageBean newImage : newProductImage) {
                if (newImage.getId() != null) {
                    newProductImageMap.put(newImage.getId(), newImage);
                }
            }

            for (ProductImageBean originProductImage : originalProductImage) {
                ProductImageBean productImage = newProductImageMap.get(originProductImage.getId());
                if (productImage == null) {
                    toDeleteProductImage.add(originProductImage.getId());
                    fileStorageService.deleteFile(originProductImage.getImage_url());
                }
            }

            if (!toDeleteProductImage.isEmpty()) {
                productRepository.deleteProductImage(toDeleteProductImage);
            }

            if (files != null && !files.isEmpty()) {
                List<ProductImageBean> productImages = new ArrayList<>();
                for (MultipartFile file : files) {
                    String originFileName = file.getOriginalFilename();
                    if (originFileName != null && !originFileName.isEmpty()) {
                        String fileExtension = originFileName.substring(originFileName.lastIndexOf("."));

                        String uniqueFileName = UUID.randomUUID().toString()
                                + "_"
                                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                                + fileExtension;

                        fileStorageService.saveProductImage(file, productBean.getId(), uniqueFileName);

                        ProductImageBean productImageBean = new ProductImageBean();
                        productImageBean.setProduct_id(productBean.getId());
                        productImageBean.setImage_url("/products/" + productBean.getId() + "/" + uniqueFileName);

                        productImages.add(productImageBean);
                    }
                }

                productRepository.createProductImage(productImages);
            }


            productRepository.updateProduct(productBean);

        } catch (Exception e) {
            this.checkException(e, res);
        }
        return res;
    }

    public ApiResponse deleteProduct(Long id) throws BaseException {
        ApiResponse res = new ApiResponse();
        try {
            fileStorageService.deleteFolder("products", id);
            productRepository.deleteProduct(id);

        } catch (Exception e) {
            this.checkException(e, res);
        }
        return res;
    }


}
