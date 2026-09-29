import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NeuronTest {
    @Test
    public void testConstructor0(){
        assertThrows(Exception.class, () -> new Neuron(new int[] {0, 1}, new double[] {0, 1, 2}));
    }

    @Test
    public void testConstructor1(){
        assertThrows(Exception.class, () -> new Neuron(new int[] {0, 1, 2}, new double[] {0, 2}));
    }

    @Test
    public void testConstructor2(){
        assertDoesNotThrow(() -> new Neuron(new int[] {0, 1, 2}, new double[] {0, 1, 2}));
    }

    @Test
    public void testCalcZ0() throws Exception {
        Neuron neuron = new Neuron(new int[] {1, 0, 0}, new double[] {-1, 0.5, 0.5});
        assertEquals(-1, neuron.calcZ());
    }

    @Test
    public void testCalcZ1() throws Exception {
        Neuron neuron = new Neuron(new int[] {1, 1, 0}, new double[] {-1, 0.5, 0.5});
        assertEquals(-0.5, neuron.calcZ());
    }

    @Test
    public void testCalcZ2() throws Exception {
        Neuron neuron = new Neuron(new int[] {1, 1, 1}, new double[] {-1, 0.5, 0.5});
        assertEquals(0, neuron.calcZ());
    }

    @Test
    public void testGFunction0() throws Exception {
        Neuron neuron = new Neuron(new int[] {1, 1, 0}, new double[] {-1, 0.5, 0.5});
        assertEquals(0, (int) Math.round(neuron.gFunction(neuron.calcZ())));
    }

    @Test
    public void testGFunction1() throws Exception {
        Neuron neuron = new Neuron(new int[] {1, 1, 1}, new double[] {-1, 0.5, 0.5});
        assertEquals(1, (int) Math.round(neuron.gFunction(neuron.calcZ())));
    }
}