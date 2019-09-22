package topica.linhnv5.trungnt9.jdbc;

import com.topica.edu.itlab.jdbc.tutorial.entity.ClassEntity;
import com.topica.edu.itlab.jdbc.tutorial.entity.StudentEntity;
import com.topica.edu.itlab.jdbc.util.EntityLoader;
import com.topica.edu.itlab.jdbc.util.MySQLConnection;

import static topica.linhnv5.trungnt9.jdbc.Config.*;

import java.util.List;

/**
 * Main class to test lazy loading
 * @author ljnk975
 */
public class EagerLoadingMain {

	public static void main(String[] args) {
		// Connect sql server
		System.out.println("Connect to database");
		try {
			MySQLConnection.gI().connect(host, dbName, user, pass);
		} catch (Exception e) {
			System.out.println("Exception when connect db: "+e);
			e.printStackTrace();
			System.exit(1);
		}

		// Get amount of time
		long time = System.currentTimeMillis();

		// Lazy loading class
		System.out.println("Eager loading");
		List<ClassEntity> listOfClass = null;
		try {
			listOfClass = EntityLoader.eagerLoading(ClassEntity.class, true);
		} catch (Exception e) {
			System.out.println("Exception when lazy loading: "+e);
			e.printStackTrace();
			System.exit(2);
		}

		// Show list
		System.out.println("Load done, total size="+listOfClass.size());
		for (ClassEntity e : listOfClass) {
			System.out.println("Class id="+e.getId()+" name="+e.getName());

			// Print list
			for (StudentEntity student : e.getListStudent())
				System.out.println("   Student id="+student.getId()+" name="+student.getName());
		}

		System.out.println("Total time="+(System.currentTimeMillis()-time)+" ms");
	}

}
