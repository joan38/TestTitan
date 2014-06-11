import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.example.GraphOfTheGodsFactory;
import com.tinkerpop.blueprints.Vertex;

import static com.tinkerpop.blueprints.Compare.*;

public class Main {

    public static void main(String[] args) {
        TitanGraph g = GraphOfTheGodsFactory.create("target/db");
        Iterable<Vertex> result = g.query().has("age", GREATER_THAN, 1000).vertices();

        System.out.println("----------------");
        for (Vertex v : result) {
            System.out.println((String) v.getProperty("name"));
        }
        System.out.println("----------------");
    }
}
