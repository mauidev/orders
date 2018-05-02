package com.overstock.orders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Singleton;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The <code>MatchEngine</code> service will apply sell and buy orders against the book.
 * <p>
 * The book is a list of sell orders and a list of buy orders. Incoming orders
 * will be matched against the appropriate list reducing quantity counts and removing
 * orders once qty for a specific price is used. Any unmatched amounts will result in
 * new orders added to the book.
 * 
 */
@Singleton
public class MatchEngine {

	private ObjectMapper mapper = new ObjectMapper();
	
	// flag to only accept orders until the first book is called
	private volatile boolean init = true;
	
	// sorted in descending order
	private volatile List<Transaction> buyOrders = new ArrayList<Transaction>();
	
	// sorted in ascending order
	private volatile List<Transaction> sellOrders = new ArrayList<Transaction>();
	
	/**
	 * Apply a sell order against buy orders starting with the highest price buy order.
	 * 
	 * @param sellOrder
	 */
	public synchronized void sell(Transaction sellOrder) {
		match(sellOrder, buyOrders, o -> addSellOrder(o));
	}
	
	/**
	 * Apply a buy order against sell orders starting with the lowest price sell order.
	 * 
	 * @param buyOrder
	 */
	public synchronized void buy(Transaction buyOrder) {
		match(buyOrder, sellOrders, o -> addBuyOrder(o));
	}
	
	/**
	 * Matching engine logic. Works for both buy and sell orders. 
	 * 
	 * The list passed in is already sorted in the correct order. This allows buys to be applied
	 * against ascending sells and sells to be applied against descending buys.
	 * 
	 * @param order the sell or buy order
	 * @param orders list of buys or sells from the book
	 * @param addOrder function to add remaining qty as a new order to the book
	 */
	private void match(Transaction order, List<Transaction> orders, Consumer<Transaction> addOrder) {
		if (init || orders.size() == 0 ){
			addOrder.accept(order);
			return;
		}
		else {
			Iterator<Transaction> iter = orders.iterator();
			while (iter.hasNext()) {
				Transaction transaction = iter.next();
				System.out.println(transaction);
				int num = transaction.getQty() - order.getQty();
				if (num == 0) {
					iter.remove();
					break;
				} else if (num > 0) {
					transaction.setQty(num);
					break;
				} else if (num < 0) {
					num = num * -1;
					iter.remove();
					order.setQty(num);
					if (orders.size() ==0) {
					 	addOrder.accept(order);
					}
				}
				
			}
			
		}
	}
	
	private void addSellOrder(Transaction sellOrder) {
		sellOrders.add(sellOrder);
		Collections.sort(sellOrders, (p1,p2) -> (p1.getPrice().doubleValue() < p2.getPrice().doubleValue() ? -1 : 1) );
	}
		
	private void addBuyOrder(Transaction buyOrder) {
		buyOrders.add(buyOrder);
		Collections.sort(buyOrders, (p1,p2) -> (p1.getPrice().doubleValue() > p2.getPrice().doubleValue() ? -1 : 1) );
	}
		
	/**
	 * Returns two json arrays keyed by "buys" and "sells".
	 * 
	 * Method is synchronized to prevent underlying changes to the buy and sell lists
	 * while serializing the lists to a json format.
	 * 
	 * <pre>
	 *   {
	 *     "buys":[{"qty":10,"prc":9.5},{"qty":10,"prc":7}],
	 *     "sells":[{"qty":10,"prc":13},{"qty":10,"prc":15}]
	 *   }
	 * </pre>
	 * 
	 * @return book serialized to json
	 * @throws JsonProcessingException if parsing error occurs
	 */
	public synchronized String book() throws JsonProcessingException {
	
		init = false;
		
		JSONObject obj = new JSONObject();
		
		JSONArray buys = new JSONArray();
		buys.addAll(buyOrders);
		
		JSONArray sells = new JSONArray();
		sells.addAll(sellOrders);
				
		obj.put("buys", buys);
		obj.put("sells", sells);
			
		String json = mapper.writeValueAsString(obj);
		return json;
	}
}
