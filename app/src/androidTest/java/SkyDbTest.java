import com.guster.skydb.Repository;
import com.guster.skydb.sample.DataContentProvider;
import com.guster.skydb.sample.domain.Student;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by Gusterwoei on 8/11/16.
 */
public class SkyDbTest {

    @Before
    public void init() {
        // load data
        DataContentProvider.loadDummyData();
    }

    @Test
    public void startTesting() {

    }
}
