package mf;

import static mf.model.Constants.D01;
import static mf.model.Constants.D02;
import static mf.model.Constants.D03;
import static mf.model.Constants.D04;
import static mf.model.Constants.D05;
import static mf.model.Constants.D06;
import static mf.model.Constants.D07;
import static mf.model.Constants.D08;
import static mf.model.Constants.D09;
import static mf.model.Constants.D10;
import static mf.model.Constants.D11;
import static mf.model.Constants.D12;
import static mf.model.Constants.D13;
import static mf.model.Constants.D14;
import static mf.model.Constants.D15;
import static mf.model.Constants.D16;
import static mf.model.Constants.D17;
import static mf.model.Constants.D18;
import static mf.model.Constants.D19;
import static mf.model.Constants.D20;
import static mf.model.Constants.D21;
import static mf.model.Constants.D22;
import static mf.model.Constants.D23;
import static mf.model.Constants.D24;
import static mf.model.Constants.D25;
import static mf.model.Constants.D26;
import static mf.model.Constants.D27;
import static mf.model.Constants.D28;
import static mf.model.Constants.D29;
import static mf.model.Constants.D30;
import static mf.model.Constants.D31;
import static mf.model.Constants.D32;
import static mf.model.Constants.D33;
import static mf.model.Constants.D34;
import static mf.model.Constants.D35;
import static mf.model.Constants.D36;
import static mf.model.Constants.D37;
import static mf.model.Constants.D38;
import static mf.model.Constants.D39;
import static mf.model.Constants.D40;
import static mf.model.Constants.D41;
import static mf.model.Constants.D42;
import static mf.model.Constants.D43;
import static mf.model.Constants.D44;
import static mf.model.Constants.D45;
import static mf.model.Constants.D46;
import static mf.model.Constants.D47;
import static mf.model.Constants.D48;
import static mf.model.Constants.D49;
import static mf.model.Constants.D50;
import static mf.model.Constants.D51;
import static mf.model.Constants.D52;
import static mf.model.Constants.D53;
import static mf.model.Constants.D54;
import static mf.model.Constants.D55;
import static mf.model.Constants.D56;
import static mf.model.Constants.D57;
import static mf.model.Constants.D58;
import static mf.model.Constants.D59;
import static mf.model.Constants.D60;
import static mf.model.Constants.D61;
import static mf.model.Constants.D62;
import static mf.model.Constants.D63;
import static mf.model.Constants.D64;
import static mf.model.Constants.D65;
import static mf.model.Constants.D66;
import static mf.model.Constants.D67;
import static mf.model.Constants.D68;
import static mf.model.Constants.D69;
import static mf.model.Constants.D70;

import java.util.Map;

import mf.model.Attribute;
import mf.model.Visitor;

public class RowsBuilder extends Visitor {
	@Override
	protected void visit(Map<String, Object> m) {
		Map<String, Object> children = Attribute.children.getChildren(m);
		m.put("row1", listWith(children,D01,D02,D03,D04,D05));
		m.put("row2", listWith(children,D06,D07,D08,D09,D10));
		m.put("row3", listWith(children,D11,D12,D13,D14,D15));
		m.put("row4", listWith(children,D16,D17,D18,D19,D20));
		m.put("row5", listWith(children,D21,D22,D23,D24,D25));
		m.put("row6", listWith(children,D26,D27,D28,D29,D30));
		m.put("row7", listWith(children,D31,D32,D33,D34,D35));
		m.put("row8", listWith(children,D36,D37,D38,D39,D40));
		m.put("row9", listWith(children,D41,D42,D43,D44,D45));
		m.put("row10", listWith(children,D46,D47,D48,D49,D50));
		m.put("row11", listWith(children,D51,D52,D53,D54,D55));
		m.put("row12", listWith(children,D56,D57,D58,D59,D60));
		m.put("row13", listWith(children,D61,D62,D63,D64,D65));
		m.put("row14", listWith(children,D66,D67,D68,D69,D70));
		

		m.put("slots", listWith(children
				,D01,D02,D03,D04,D05
				,D06,D07,D08,D09,D10
				,D11,D12,D13,D14,D15
				,D16,D17,D18,D19,D20
				,D21,D22,D23,D24,D25
				,D26,D27,D28,D29,D30));
	}
}
