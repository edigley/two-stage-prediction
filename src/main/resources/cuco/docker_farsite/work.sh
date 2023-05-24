#########  READ ENV VARIABLES ###########
echo "reading env variables"
eval `./jsonenv.py < credentials.json`
aws sqs list-queues --region ${REGION}
eval `aws sqs receive-message --queue-url ${QUEUE_URL} --region ${REGION} | jq -r -c '.Messages[0].Body' | jq '.' | ./jsonenv.py`

#########  DOWNLOAD FILE ###########
echo "downloading"
aws s3 cp s3://${S3_INPUT_BUCKET}/${S3_INPUT_DIRNAME}/ . --recursive

ORIG=/mnt/disk2/Helipistes/SIMULATION/${S3_INPUT_DIRNAME}/
NEW=`pwd`"/"
sed -i "s|$ORIG|$NEW|g" Settings_simulation.txt

############  CONVERT FILE   #######
echo "running farsite"

./farsite4P -i Settings_simulation.txt

############  REUPLOAD   ###########
echo "uploading"

aws s3 cp OUTPUT/ s3://$S3_OUTPUT_BUCKET/$S3_OUTPUT_DIRNAME/OUTPUT --recursive
