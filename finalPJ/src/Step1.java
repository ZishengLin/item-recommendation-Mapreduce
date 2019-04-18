import java.io.IOException;
import java.util.Iterator;
import java.util.Map;



import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import au.com.bytecode.opencsv.CSVParser;

public class Step1 {
    public static class Step1_ItemMapper extends Mapper<Object, Text, Text, Text>{
        private CSVParser csvParser = new CSVParser();

        public void map(Object KEY, Text value, Context context) throws IOException, InterruptedException {
            String[] line = this.csvParser.parseLine(value.toString());
            String userID = line[0];
            String movieID = line[1];
            String rating = line[2];

            context.write(new Text(userID), new Text(movieID + ":" + rating));
        }

    }

    public static class Step1_ItemReducer extends Reducer<Text, Text, Text, Text> {

        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            StringBuilder sb = new StringBuilder();
            for (Text t: values) {
                sb.append(",");
                sb.append(t.toString());
            }
            sb.delete(0,1);
            context.write(key, new Text(sb.toString()));
        }

    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args)
                .getRemainingArgs();

        Job job = new Job(conf, "Step1");
        job.setJarByClass(Step1.class);
        job.setMapperClass(Step1_ItemMapper.class);
        job.setReducerClass(Step1_ItemReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
