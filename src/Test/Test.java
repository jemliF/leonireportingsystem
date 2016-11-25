package Test;

import javafx.beans.property.BooleanProperty;

/**
 *
 * @author bewa1022
 */
public class Test {

    private String name;
    private boolean test;
    private BooleanProperty testProperty;

    public boolean isTest() {
        return test;
    }

    public BooleanProperty testProperty() {
        return testProperty;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public BooleanProperty getTestProperty() {
        return testProperty;
    }

    public void setTestProperty(BooleanProperty testProperty) {
        this.testProperty = testProperty;
    }

    public Test(String name, boolean test) {
        this.name = name;
        this.test = test;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
