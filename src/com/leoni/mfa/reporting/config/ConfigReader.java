package com.leoni.mfa.reporting.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author bewa1022
 */
public class ConfigReader {

    public static List<Process> getProcesses() {
        SAXBuilder builder = new SAXBuilder();
        List<Process> processList = new ArrayList<Process>();
        try {
            Document doc = builder.build(new File("./config.xml"));
            Element root = doc.getRootElement();
            Element processes = root.getChild("processes");
            for (Element process : processes.getChildren()) {
                processList.add(new Process(process.getAttributeValue("operation"), process.getAttributeValue("route_step"), process.getAttributeValue("segment"),
                        Boolean.parseBoolean(process.getAttributeValue("ISH")), Boolean.parseBoolean(process.getAttributeValue("ITSH"))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processList;
    }

    public static List<WorkShift> getWorkShifts() {
        SAXBuilder builder = new SAXBuilder();
        List<WorkShift> workShiftList = new ArrayList<WorkShift>();
        try {
            Document doc = builder.build(new File("./config.xml"));
            Element root = doc.getRootElement();
            Element shifts = root.getChild("shifts");
            for (Element shift : shifts.getChildren()) {
                workShiftList.add(new WorkShift(Integer.parseInt(shift.getAttributeValue("number")), Boolean.parseBoolean(shift.getAttributeValue("enabled"))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return workShiftList;
    }

    public static void main(String[] args) {
        System.out.println(new ConfigReader().getProcesses().size());
        System.out.println(new ConfigReader().getWorkShifts().size());
    }
}
