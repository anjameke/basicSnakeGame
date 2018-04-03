package cse417;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleFunction;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static cse417.GraphUtils.EPSILON;


/**
 * Program that reads a table of numbers stored in a CSV and writes a new CSV
 * with the entries in the table rounded in such a way that the colum and row
 * sums are equal to the correct sums rounded.
 */
public class TableRounder {

  /** Entry point for a program to round table entries. */
  public static void main(String[] args) throws IOException {
    ArgParser argParser = new ArgParser("Table");
    argParser.addOption("header", Boolean.class);
    argParser.addOption("digits", Integer.class);
    argParser.addOption("out-file", String.class);
    args = argParser.parseArgs(args, 1, 1);

    // If the user asks us to round to a digit after the decimal place, we
    // multiply by a power of 10 so that rounding integers after scaling is the
    // same as rounding at the desired decimal place. (We scale back below.)
    int digits = argParser.hasOption("digits") ?
        argParser.getIntegerOption("digits") : 0;
    final double scale = Math.pow(10, digits);

    CsvParser csvParser = new CsvParser(args[0]);
    String[] header = null;
    if (argParser.hasOption("header")) {
      assert csvParser.hasNext();
      header = csvParser.next();
    }

    // Read the table from the CSV.
    List<double[]> table = new ArrayList<double[]>();
    while (csvParser.hasNext()) {
      table.add(Arrays.asList(csvParser.next()).stream()
          .mapToDouble(s -> scale * Double.parseDouble(s)).toArray());
      if (table.size() > 2) {
        assert table.get(table.size()-2).length ==
               table.get(table.size()-1).length;
      }
    }

    roundTable(table);

    // Output the rounded tables.
    PrintStream output = !argParser.hasOption("out-file") ? System.out :
        new PrintStream(new FileOutputStream(
            argParser.getStringOption("out-file")));
    if (header != null)
      writeRow(output, header);  // echo the header to the output
    for (double[] vals : table) {
      writeRow(output,
          DoubleStream.of(vals).map(v -> v / scale).toArray(), digits);
    }
  }

  /** Modifies the given table so that each entry is rounded to an integer. */
  static void roundTable(final List<double[]> table) {
    if (table.size() == 0) return;

    // TODO: implement this
    
    // create bipartite graph
    ArrayList<Integer> nodeList = new ArrayList<Integer>();		// list to hold all the col/row nodes
    int source = -2;
    int sink = -1;
    nodeList.add(source);
    nodeList.add(sink);
    for (int i = 0; i < table.size(); i++) {			// column nodes
    	nodeList.add(i);
    }
    for (int i = 0; i < table.size(); i++) {			// row nodes, because it can't have the same
    													// value as the column node, in terms of confusion
    	nodeList.add(100 + i);
    }
   
    ToDoubleBiFunction<Integer, Integer> minEdgeFlow = (u, v) -> {
    	if (u == source && v == sink) {
    		return 0.0;
    	} else if (u == source && v != sink) {			// source -> column
    		if (v != source) {
    			if (v < 100) {							// v is a column node
	    			double sum = 0.0;
	    			for (int i = 0; i < table.size(); i++) {
	    				sum += table.get(i)[v];
	    				return Math.floor(sum);
	    			}
    			}
    		}
    	} else if (u != source && v == sink) {			// row -> sink
    		if (u != sink) {
    			if (u > 100) {							// u is a row node
	    			double sum = 0.0;
	    			for (int i = 0; i < table.get(0).length; i++) {
	    				sum += table.get(u)[i];
	    				return Math.floor(sum);
	    			}
    			}
    		}
    	} else if (u != source && v != sink && u != sink && v != source) {		// column -> row
    		if (u > 100 && v < 100 || u < 100 && v > 100) {						// u and v must be column/row pair
	    		double capacity = 0.0;
	    		capacity = table.get(u)[v];
	    		return Math.floor(capacity);
    		}
    	}
    	return 0.0;
    };
    
    ToDoubleBiFunction<Integer, Integer> maxEdgeFlow = (u, v) -> {
    	if (u == source && v == sink) {
    		return 0.0;
    	} else if (u == source && v != sink) {			// source -> column
    		if (v != source) {
    			if (v < 100) {							// v is a column node
	    			double sum = 0.0;
	    			for (int i = 0; i < table.size(); i++) {
	    				sum += table.get(i)[v];
	    				return Math.floor(sum);
	    			}
    			}
    		}
    	} else if (u != source && v == sink) {			// row -> sink
    		if (u != sink) {
    			if (u > 100) {							// u is a row node
	    			double sum = 0.0;
	    			for (int i = 0; i < table.get(0).length; i++) {
	    				sum += table.get(u)[i];
	    				return Math.floor(sum);
	    			}
    			}
    		}
    	} else if (u != source && v != sink && u != sink && v != source) {		// column -> row
    		if (u > 100 && v < 100 || u < 100 && v > 100) {						// u and v must be column/row pair
	    		double capacity = 0.0;
	    		capacity = table.get(u)[v];
	    		return Math.floor(capacity);
    		}
    	}
    	return 0.0;
    };
    
    // call findFeasibleBoundedFlow()
    ToDoubleBiFunction<Integer, Integer> feasibleFlow = findFeasibleBoundedFlow(source, sink, nodeList, minEdgeFlow,
    													maxEdgeFlow);
    if (feasibleFlow == null) {
    	return;
    }
    ToDoubleBiFunction<Integer, Integer> max = GraphUtils.maxFlow(source, sink, nodeList, minEdgeFlow, maxEdgeFlow,
    															feasibleFlow);
    // replace entries in the tables with the flows between edges
    
    // iterate through the flow
    for (int i = 0; i < table.size(); i++) {
    	for (int j = 0; j < table.get(i)[0]; j++) {
    		table.get(i)[j] = max.applyAsDouble(i, j + 100);
    	}
    }    
  }

  /**
   * Returns a flow that satisfies the given constraints or null if none
   * exists.
   */
  static ToDoubleBiFunction<Integer, Integer> findFeasibleBoundedFlow(
      final Integer source, final Integer sink, Collection<Integer> nodes,
      ToDoubleBiFunction<Integer, Integer> minEdgeFlow,
      ToDoubleBiFunction<Integer, Integer> maxEdgeFlow) {

    // TODO: implement this properly
	  
	// make max flow = outgoing flow - incoming flow
	// First create the edge between source and sink to positive infinity
	ToDoubleBiFunction<Integer, Integer> previousCapacity = (u, v) -> maxEdgeFlow.applyAsDouble(u, v) -
															minEdgeFlow.applyAsDouble(u, v);
	ToDoubleBiFunction<Integer, Integer> capacity = (u, v) -> (u == sink && v == source || u == source && v == sink) ? 
					   Double.MAX_VALUE : previousCapacity.applyAsDouble(u, v);
	
	// make demands on each node = min flow going in + all min flow going out
	ToDoubleFunction<Integer> demand = u -> {
		return -GraphUtils.imbalanceAt(u, nodes, minEdgeFlow);
				};
	
	// call findFeasibleDemandFlow 
	ToDoubleBiFunction<Integer, Integer> feasibleFlow = findFeasibleDemandFlow(nodes, capacity, demand);
	
	// add back in the minEdgeFlow
	ToDoubleBiFunction<Integer, Integer> boundedFlow = (u, v) -> feasibleFlow.applyAsDouble(u, v) 
						 + minEdgeFlow.applyAsDouble(u, v);
	if (feasibleFlow != null) {
		return boundedFlow;
	}
	return null;
  }

  /**
   * Returns a circulation that satisfies the given capacity constraints (upper
   * bounds) and demands or null if none exists.
   */
  static ToDoubleBiFunction<Integer, Integer> findFeasibleDemandFlow(
      Collection<Integer> nodes,
      final ToDoubleBiFunction<Integer, Integer> capacity,
      final ToDoubleFunction<Integer> demand) {

    // Make sure that the demands could even possibly be met.
	// Make sure that the demands could even possibly be met.
	    double surplus = 0, deficit = 0;
	    for (Integer n : nodes) {
	      if (demand.applyAsDouble(n) >= EPSILON)
	        surplus += demand.applyAsDouble(n);
	      if (demand.applyAsDouble(n) <= -EPSILON)
	        deficit += -demand.applyAsDouble(n);
	    }
	    assert Math.abs(surplus - deficit) <= 1e-5;

	    // TODO: implement this properly

	    // introduce sink node t
	    // introduce source node s
	    ArrayList<Integer> copiedNodes = new ArrayList<Integer>();
	    int source = -1;
	    int sink = -2;
	    copiedNodes.add(source);    // source node s
	    copiedNodes.add(sink);     // sink node t
	    copiedNodes.addAll(nodes);

	    ToDoubleBiFunction<Integer, Integer> flow = (a, b) -> {
	      if (a == b) {
	        return 0.0;
	      }
	      if (a == source && b == sink) {
	        return 0.0;
	      } else if (a == sink && b == source) {
	        return Double.POSITIVE_INFINITY;
	      } else if (a == source && b != sink) {
	        if (b != source) {
	          if (demand.applyAsDouble(b) < 0) {
	            return -demand.applyAsDouble(b);
	          }
	        }
	      } else if (a != source && b == sink) {
	        if (a != sink) {
	          if (demand.applyAsDouble(a) > 0) {
	            return demand.applyAsDouble(a);
	          }
	        }
	      } else if (a != source && b != sink && a != sink && b != source) {
	           return capacity.applyAsDouble(a, b);
	        }
	      return 0.0;
	    };
  
    ToDoubleBiFunction<Integer, Integer> max = GraphUtils.maxFlow(source, sink, copiedNodes, flow);
 //   double flows = GraphUtils.flowValue(source, sink, copiedNodes, max);
    if (Math.abs(Math.abs(GraphUtils.flowValue(source, sink, copiedNodes, max)) - Math.abs(surplus)) < 1e-5) {
      return max;
    } else {
      return null;
    }
  }
 
  /**
   * Outputs a CSV row of the given values with the specified number of digits
   * after the decimal.
   */
  private static void writeRow(PrintStream out, double[] vals, int digits) {
    final String fmt = String.format("%%.%df", digits);
    DoubleFunction<String> fmtVal = v -> String.format(fmt, v);
    writeRow(out, DoubleStream.of(vals).mapToObj(fmtVal)
        .toArray(n -> new String[n]));
  }

  /**
   * Outputs a CSV row containing the given values. Note that the current
   * implementation assumes that there are no commas in the column values.
   */
  private static void writeRow(PrintStream out, String[] row) {
    for (int i = 0; i < row.length; i++)
      assert row[i].indexOf(',') < 0;  // quoting not supported here

    out.println(Stream.of(row).collect(Collectors.joining(",")).toString());
  }
}
