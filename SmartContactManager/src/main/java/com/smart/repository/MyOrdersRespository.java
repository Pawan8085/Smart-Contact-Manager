package com.smart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart.entities.MyOrder;

public interface MyOrdersRespository extends JpaRepository<MyOrder, Long>{
	
	public MyOrder findByOrderId(String orderId);
}
