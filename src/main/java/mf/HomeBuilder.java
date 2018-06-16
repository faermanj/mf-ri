package mf;

import static mf.model.Attribute.head_class;
import static mf.model.Attribute.home;
import static mf.model.Attribute.key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import mf.model.Attribute;
import mf.model.Visitor;


public class HomeBuilder extends Visitor {
	Map< /* Row */ Integer, Map< Integer, /*Slot*/ List< /* Item */ Map<String,Object>>>> house = new TreeMap<>();
	
	@Override
	protected void visit(Map<String, Object> m) {	
		if (home.contains(m)){
			String h = home.getString(m);
			place(m,h);
		}
	}

	private static final Integer slotOf(Integer p) {
		return p % 6;
	}

	private static final Integer rowOf(Integer h) {
		Integer block = (h / 6);
		Integer s = slotOf(h);
		Integer slice = (s > 1) ? 1:0;
		//System.out.format("h %d => %d  %d %d \n",h,block,s,slice);
		return (2*block) + slice;
	}
	
	private void place(Map<String, Object> m, String hi) {
		String k = key.getString(m);
		if(hi.length()>0 && hi.toUpperCase().startsWith("D")){
			String substring = hi.substring(1);	
			Integer h = 1;
			try{
				h = Integer.valueOf(substring);
			}catch(NumberFormatException e){
				execution.addError("["+k+"] "+e.getMessage());
			}
			h--;
			List< Map<String,Object>> slot = getSlot(h);
			if(slot.isEmpty()){
				Attribute.head_class.put(m,"head");
			}else{
				Attribute.head_class.put(m,"tail");
			}
			slot.add(m);
		}
	}

	private List<Map<String, Object>> getSlot(Integer h) {
		Integer r = rowOf(h);
		Map< Integer, List< Map<String,Object>>> row = getRow(r);		
		Integer s = slotOf(h);		
		List<Map<String, Object>> slot = row.get(s);
		if (slot  == null) {
			slot = new ArrayList<>();
			row.put(s, slot);
		}	
		return slot;
	}

	private Map<Integer, List<Map<String, Object>>> getRow(Integer r) {
		Map<Integer, List<Map<String, Object>>> row = house.get(r);
		if(row == null){
			row = new TreeMap<>();
			house.put(r, row);
		}
		return row;
	}
	
	@Override
	protected void goodbye() {
		List<List<List<Map<String,Object>>>> rows = houseAsList();
		execution.putHome("rows",rows);
	}
	
	
	private List<List<List<Map<String, Object>>>> houseAsList() {
		List<List<List<Map<String, Object>>>> rows = new ArrayList<>();
		
		Set<Entry<Integer, Map<Integer, List<Map<String, Object>>>>> houseEntries = house.entrySet();
		for (Entry<Integer, Map<Integer, List<Map<String, Object>>>> houseEntry : houseEntries) {
			Integer r = houseEntry.getKey();			
			Map<Integer, List<Map<String, Object>>> row = houseEntry.getValue();
			List<List<Map<String, Object>>> _row = new ArrayList<>();
			Set<Entry<Integer, List<Map<String, Object>>>> rowEntries = row.entrySet();
			for (Entry<Integer, List<Map<String, Object>>> rowEntry : rowEntries) {
				Integer s = rowEntry.getKey();
				List<Map<String, Object>> _slot = new ArrayList<>();
				List<Map<String, Object>> slot = rowEntry.getValue();				
				for (int i = 0; i < slot.size(); i++) {					
					Map<String,Object> m = slot.get(i);
					_slot.add(m);
					String k = key.getString(m);
					String h = home.getString(m);
					String hh = head_class.getString(m);
					//System.out.format(" H %s R %d S %d I %d h %s K %s\n",h,r,s,i,hh,k);
				}
				_row.add(_slot);
			}
			rows.add(_row);
		}

		return rows;
	}

	public static void main(String[] args) {
		for (int i=0;i<14;i++){
			System.out.println(rowOf(i));
		}
	}
}
