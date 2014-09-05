package test;

import org.junit.Before;
import org.junit.Test;

import java.lang.invoke.LambdaMetafactory;

import static org.junit.Assert.assertEquals;

public class TestMethodHandles {
    @Before
    public void setUp() {
    }

    @Test
    public void methodHandlesToSameObjectShouldBeEqual() throws Throwable {
        VoidVoid v1 = LambdaMetafactory this::setUp;
        VoidVoid v2 = this::setUp;
        assertEquals( v1, v2 );
    }


    @FunctionalInterface
    public interface VoidVoid {
        void voidMethod();
    }

}
