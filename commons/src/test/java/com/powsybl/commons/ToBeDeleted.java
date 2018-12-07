package com.powsybl.commons;

import com.powsybl.commons.io.table.AsciiTableFormatter;
import org.nocrala.tools.texttablefmt.BorderStyle;
import org.nocrala.tools.texttablefmt.CellStyle;
import org.nocrala.tools.texttablefmt.Table;

import java.io.IOException;

public class ToBeDeleted {
    public static void main(String[] args) {

        System.out.println("hey");


        Table table = new Table(5, BorderStyle.CLASSIC_WIDE);
        CellStyle centerStyle = new CellStyle(CellStyle.HorizontalAlign.center);

        table.addCell("");
        table.addCell("Main CC connected/disconnected", 2);
        table.addCell("Others CC connected/disconnected", 2);

        System.out.println(table.render());


        AsciiTableFormatter formatter = new AsciiTableFormatter("myFormatter",5);


        try {

            formatter.writeCell("Bus count").
                    writeCellWithColspan("Main CC connected/disconnected",2).
                    writeCellWithColspan("Others CC connected/disconnected",2);

            formatter.writeCell("Bus count").
                    writeCellWithColspan("yes",2).writeCellWithColspan("hey",2);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(formatter.getTable().render());



    }
}
