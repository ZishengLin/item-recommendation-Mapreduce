import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import au.com.bytecode.opencsv.CSVParser;

public class Step5 {
    public static class Step5_Mapper extends Mapper<Object, Text, Text, DoubleWritable> {
        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] line = value.toString().split("\t");
            String[] userRatings = line[3].split(",");
            String[] movies = line[0].split(":");
            int number = Integer.parseInt(line[1]);

            for(String userRating: userRatings) {
                String userId = userRating.split(":")[0];
                double rating = Double.parseDouble(userRating.split(":")[1]);

                context.write(new Text(userId + ":" + movies[1]), new DoubleWritable(rating * number));
            }
        }
    }

    public static class Step5_Reducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        public void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
            double result = 0;
            for(DoubleWritable v: values) {
                result += v.get();
            }
            context.write(key, new DoubleWritable(result));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args)
                .getRemainingArgs();

        Job job = new Job(conf, "Step5");
        job.setNumReduceTasks(10);
        job.setJarByClass(Step1.class);
        job.setMapperClass(Step5_Mapper.class);
        job.setReducerClass(Step5_Reducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(DoubleWritable.class);

        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
