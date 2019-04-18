import java.io.IOException;
import java.util.Iterator;
import java.util.Map;



import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import au.com.bytecode.opencsv.CSVParser;

public class Step2 {
    public static class Step2_Mapper extends Mapper<Object, Text, Text, LongWritable> {
        private CSVParser csvParser = new CSVParser();
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] line = value.toString().split("\t");
            String[] data = line[1].split(",");

            for(int i = 0; i < data.length; i++) {
                String itemId1 = data[i].split(":")[0];
                for(int j = 0; j < data.length; j++) {
                    String itemId2 = data[j].split(":")[0];
                    context.write(new Text(itemId1 + ":" + itemId2), new LongWritable(1));
                }
            }
        }
    }

    public static class Step2_Reducer extends Reducer<Text, LongWritable, Text, LongWritable> {
        public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
            int result = 0;
            for(LongWritable v: values) {
                result += v.get();
            }

            context.write(key, new LongWritable(result));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args)
                .getRemainingArgs();

        Job job = new Job(conf, "Step2");
        job.setJarByClass(Step1.class);
        job.setMapperClass(Step2_Mapper.class);
        job.setReducerClass(Step2_Reducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);

        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
