package me.onenrico.yourauction.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.inventory.ItemStack;

import me.onenrico.yourauction.utils.ItemUT;
import me.onenrico.yourauction.utils.StringUT;

public class PlaceholderManager {
	final static String start = "{";
	final static String end = "}";

	private HashMap<String, Object> values = new HashMap<>();
	private StrSubstitutor processor;
	
	private StrSubstitutor bridgeprocessor;
	private StrLookup bridge;
	private HashMap<String,StrLookup> placeholders = new HashMap<>();
	

	@SuppressWarnings("unchecked")
	public PlaceholderManager(PlaceholderManager old) {
		setValues((HashMap<String, Object>) old.getValues().clone());
		setPlaceholders((HashMap<String, StrLookup>) old.getPlaceholders().clone());
		setProcessor(new StrSubstitutor(this.values, start, end, '!'));
		bridge = new StrLookup() {
			@Override
			public String lookup(String msg) {
				StrLookup lookup = placeholders.getOrDefault(msg, null);
				if(lookup != null) {
					return lookup.lookup(msg);
				}
				return "!{"+msg+"}";
			}
		};
		setBridgeprocessor(new StrSubstitutor(bridge,start,end,'!'));
	}
	public PlaceholderManager() {
		setProcessor(new StrSubstitutor(values, start, end, '!'));
		bridge = new StrLookup() {
			@Override
			public String lookup(String msg) {
				StrLookup lookup = placeholders.getOrDefault(msg, null);
				if(lookup != null) {
					return lookup.lookup(msg);
				}
				return "!{"+msg+"}";
			}
		};
		setBridgeprocessor(new StrSubstitutor(bridge,start,end,'!'));
		return;
	}
	public String get(String placeholder) {
		return ""+values.getOrDefault(placeholder,"");
	}
	public void add(String placeholder, Object msg) {
		values.put(placeholder, msg);
		setProcessor(new StrSubstitutor(values,start,end,'!'));
	}

	public void add(String placeholder, StrLookup msg) {
		placeholders.put(placeholder, msg);
	}

	public void remove(String placeholder) {
		values.remove(placeholder);
		setProcessor(new StrSubstitutor(values,start,end,'!'));
		placeholders.remove(placeholder);
	}

	public PlaceholderManager(HashMap<String, Object> values) {
		this.values = values;
		processor = new StrSubstitutor(values, start, end, '!');
	}

	public static PlaceholderManager custom(HashMap<String, StrLookup> values2) {
		PlaceholderManager pm = new PlaceholderManager();
		pm.setPlaceholders(values2);
		return pm;
	}

	public String process(String text) {
		if (text == null) {
			return "";
		}
		String result = processor.replace(text);
		if (bridgeprocessor != null) {
			result = bridgeprocessor.replace(result);
		}
		return StringUT.t(result);
	}

	public List<String> process(List<String> text) {
		if (text == null) {
			return new ArrayList<>();
		}
		for (int i = 0; i < text.size(); i++) {
			text.set(i, process(text.get(i)));
		}
		return text;
	}
	public ItemStack process(ItemStack item) {
		ItemUT.changeDisplayName(item, process(ItemUT.getName(item)));
		ItemUT.changeLore(item, process(ItemUT.getLore(item)));
		return item;
	}

	public HashMap<String, Object> getValues() {
		return values;
	}

	public void setValues(HashMap<String, Object> values) {
		this.values = values;
	}

	public StrSubstitutor getProcessor() {
		return processor;
	}

	public void setProcessor(StrSubstitutor processor) {
		this.processor = processor;
	}

	public StrSubstitutor getBridgeprocessor() {
		return bridgeprocessor;
	}

	public void setBridgeprocessor(StrSubstitutor bridgeprocessor) {
		this.bridgeprocessor = bridgeprocessor;
	}

	public StrLookup getBridge() {
		return bridge;
	}

	public void setBridge(StrLookup bridge) {
		this.bridge = bridge;
	}

	public HashMap<String, StrLookup> getPlaceholders() {
		return placeholders;
	}

	public void setPlaceholders(HashMap<String, StrLookup> placeholders) {
		this.placeholders = placeholders;
	}

}
