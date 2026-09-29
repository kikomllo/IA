
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ArrayCfg.
 */
public class ArrayCfgTest {

    @Test
    void toString_elementsWithSpaces() {
        ArrayCfg cfg = new ArrayCfg("1 2 3");
        assertEquals("1 2 3", cfg.toString());
    }

    @Test
    void equalsAndHashCode_testContentBased() {
        ArrayCfg a = new ArrayCfg("3 1 2");
        ArrayCfg b = new ArrayCfg(new int[]{3, 1, 2});

        assertEquals(a, b, "Equal configurations");
        assertEquals(a.hashCode(), b.hashCode(), "Equal hash codes");
    }

    @Test
    void numberSwapper_swapIndices() {
        ArrayCfg helper = new ArrayCfg("0 0 0");
        int[] arr = new int[]{1, 2, 3};
        int[] swapped = helper.numberSwapper(arr, new int[]{0, 2});

        assertSame(arr, swapped, "should return mutated array");
        assertArrayEquals(new int[]{3, 2, 1}, swapped);
    }

    @Test
    void children_generateAllSwaps_nSetK() {
        ArrayCfg parent = new ArrayCfg("1 2 3");
        // For size=3, we expect C(3,2)=3 children: (0,1), (0,2), (1,2)
        List<Ilayout> kids = parent.children();

        assertEquals(3, kids.size(), "Should be 3 children");

        // Parity rules (from parent values):
        // swap(0,1): 1(odd) & 2(even) -> k=11
        // swap(0,2): 1(odd) & 3(odd)  -> k=20
        // swap(1,2): 2(even) & 3(odd) -> k=11

        boolean bool_2_1_3 = false;
        boolean bool_3_2_1 = false;
        boolean bool_1_3_2 = false;

        for (Ilayout l : kids) {
            ArrayCfg child = (ArrayCfg) l;
            switch (child.toString()) {
                case "2 1 3": // swap 0<->1
                    bool_2_1_3 = true;
                    assertEquals(11.0, child.getK(), "Odd-even, should be k == 11");
                    break;
                case "3 2 1": // swap 0<->2
                    bool_3_2_1 = true;
                    assertEquals(20.0, child.getK(), "Odd-odd, should be k == 20");
                    break;
                case "1 3 2": // swap 1<->2
                    bool_1_3_2 = true;
                    assertEquals(11.0, child.getK(), "Even-odd, should be k == 11");
                    break;
                default:
                    fail("Unexpected child config: " + child);
            }
        }

        assertTrue(bool_2_1_3 && bool_3_2_1 && bool_1_3_2, "All children should be present");
    }

    @Test
    void isGoal_testEquals() {
        ArrayCfg a = new ArrayCfg("4 5 6");
        ArrayCfg b = new ArrayCfg("4 5 6");
        ArrayCfg c = new ArrayCfg("6 5 4");

        assertTrue(a.isGoal(b), "Equal layouts should be goal");
        assertFalse(a.isGoal(c), "Different layouts should not be goal");
    }

    @Test
    void clone_deepCopyArray_andPreserveK() throws Exception {
        ArrayCfg original = new ArrayCfg("7 8 9");
        original.setK(42.0);

        ArrayCfg clone = original.clone();

        assertEquals(original.toString(), clone.toString(), "Clone should have same content");
        assertEquals(original.getK(), clone.getK(), "Clone should preserve k value");

        Field dataField = ArrayCfg.class.getDeclaredField("data");
        dataField.setAccessible(true);

        int[] originalArray = (int[]) dataField.get(original);
        int[] cloneArray = (int[]) dataField.get(clone);

        // Arrays must be different
        assertNotSame(originalArray, cloneArray, "Clone must have own array");

        // Mutate the clone's array and ensure original is unaffected
        cloneArray[0] = 999;

        // toString reflects internal array, so they must now differ
        assertNotEquals(original.toString(), clone.toString(),
                "Change clone array must not affect original");
    }
}
