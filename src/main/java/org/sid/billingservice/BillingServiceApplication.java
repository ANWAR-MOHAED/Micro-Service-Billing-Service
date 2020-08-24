package org.sid.billingservice;

import java.util.Collection;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

@Entity
class Bill{
	
	@Id @GeneratedValue
	private Long id;
	private Date billingDate;
	@JsonProperty(access = Access.WRITE_ONLY)
	private Long customerID;
	@Transient
	private Customer customer;
	@OneToMany(mappedBy = "bill")
	private Collection<ProductItem>productItem;
	public Bill() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Bill(Long id, Date billingDate, Long customerID, Collection<ProductItem> productItem) {
		super();
		this.id = id;
		this.billingDate = billingDate;
		this.customerID = customerID;
		this.productItem = productItem;
	}
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getBillingDate() {
		return billingDate;
	}
	public void setBillingDate(Date billingDate) {
		this.billingDate = billingDate;
	}
	public Long getCustomerID() {
		return customerID;
	}
	public void setCustomerID(Long customerID) {
		this.customerID = customerID;
	}
	public Collection<ProductItem> getProductItem() {
		return productItem;
	}
	public void setProductItem(Collection<ProductItem> productItem) {
		this.productItem = productItem;
	}
	@Override
	public String toString() {
		return "Bill [id=" + id + ", billingDate=" + billingDate + ", customerID=" + customerID + ", productItem="
				+ productItem + "]";
	}
	
	
	
}
@RepositoryRestResource
interface BillRepository extends JpaRepository<Bill, Long>{}

@Projection(name = "fullBill" ,types = Bill.class)
interface BillProjection{
	public Long getId();
	public Date getBillingDate();
	public Long getCustomerID();
	public Collection<ProductItem>getProductItem();
}

@Entity
class ProductItem{
	
	@Id @GeneratedValue
	private Long id;
	@JsonProperty(access = Access.WRITE_ONLY)
	private Long productID;
	 @Transient
	private Product product;
	private double price;
	private double quantitiy;
	@ManyToOne
	@JsonProperty(access = Access.WRITE_ONLY)
	private Bill bill;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getProductID() {
		return productID;
	}
	public void setProductID(Long productID) {
		this.productID = productID;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getQuantitiy() {
		return quantitiy;
	}
	public void setQuantitiy(double quantitiy) {
		this.quantitiy = quantitiy;
	}
	public Bill getBill() {
		return bill;
	}
	public void setBill(Bill bill) {
		this.bill = bill;
	}
	public ProductItem() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ProductItem(Long id, Long productID, double price, double quantitiy, Bill bill) {
		super();
		this.id = id;
		this.productID = productID;
		this.price = price;
		this.quantitiy = quantitiy;
		this.bill = bill;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	@Override
	public String toString() {
		return "ProductItem [id=" + id + ", productID=" + productID + ", price=" + price + ", quantitiy=" + quantitiy
				+ ", bill=" + bill + "]";
	}
	
	 
}

@RepositoryRestResource
interface ProductItemRepository extends JpaRepository<ProductItem, Long>{}


class Customer{
	private Long id;
	private String name;
	private String email;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
}
@FeignClient(name = "customer-service")
interface CustomerService{
	
	@GetMapping("customers/{id}")
	public Customer findCustomerById(@PathVariable(name = "id")Long id);
}

class Product{
	private Long id;
	private String name;
	private double price;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	
}

@FeignClient(name = "INVENTORY-SERVICE")
interface InventoryService {
	
	@GetMapping("/products/{id}")
	public Product findProductById(@PathVariable(name = "id")Long id);
	
	@GetMapping("/products")
	public PagedModel<Product>findAllProduct();
}
@SpringBootApplication
@EnableFeignClients
public class BillingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingServiceApplication.class, args);
	}
	@Bean
	CommandLineRunner start(BillRepository billRepository,
			ProductItemRepository productItemRepository,
			CustomerService customerService,
			InventoryService inventoryService
			) {
		return args->{
			
			Customer c1=customerService.findCustomerById(1L);
			System.out.println("************************");
			System.out.println("get ID : "+c1.getId());
			System.out.println("get Name : "+c1.getName());
			System.out.println("get Email : "+c1.getEmail());
			System.out.println("************************");
			
			Bill bill1=billRepository.save(new Bill(null, new Date(), c1.getId(), null));
			
			
			PagedModel<Product> products=inventoryService.findAllProduct();
			products.getContent().forEach(p ->{productItemRepository.save(new ProductItem(null, p.getId(), p.getPrice(), 60, bill1));});
			
			
			
		
			};
	}
	@RestController
	class BillRestController{
		//accseder la base donne locale
		@Autowired
		private BillRepository billRepository;
		@Autowired
		private ProductItemRepository itemRepository;
		//donne en destence
		@Autowired
		private CustomerService customerService;
		@Autowired
		private InventoryService inventoryService;
		
		@GetMapping("/fullBill/{id}")
		public Bill getBill(@PathVariable(name="id")Long id) {
			Bill bill=billRepository.findById(id).get();
			bill.setCustomer(customerService.findCustomerById(bill.getCustomerID()));
			bill.getProductItem().forEach(pi->{
				pi.setProduct(inventoryService.findProductById(pi.getProductID()));
			});
			return bill;
		}
	}

}
