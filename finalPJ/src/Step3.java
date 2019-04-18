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
/*
    input comes from step 1, generates an relationship between movie and users
 */
public class Step3 {
    public static class Step3_Mapper extends Mapper<Object, Text, LongWritable,Text> {
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] line = value.toString().split("\t");
            String userId = line[0];
            String[] data = line[1].split(",");
            for(int i = 0; i < data.length; i++) {
                String itemId = data[i].split(":")[0];
                String rate = data[i].split(":")[1];

                context.write(new LongWritable(Long.parseLong(itemId)), new Text(userId + ":" + rate));
            }

        }

    }

    public static class Step3_Reducer extends Reducer<LongWritable, Text, LongWritable, Text> {
        public void reduce(LongWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            StringBuilder sb = new StringBuilder();
            for(Text v: values) {
                sb.append(",");
                sb.append(v.toString());
            }
            sb.delete(0, 1);
            context.write(key, new Text(sb.toString()));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args)
                .getRemainingArgs();

        Job job = new Job(conf, "Step3");
        job.setJarByClass(Step1.class);
        job.setMapperClass(Step3_Mapper.class);
        job.setReducerClass(Step3_Reducer.class);
        job.setMapOutputKeyClass(LongWritable.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
