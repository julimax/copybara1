# copybara1
https://central.sonatype.org/publish/requirements/


./gradlew clean build
./gradlew run
java -jar build/libs/copybara-1.0.7.jar 

 org-set-test/copybara1-fork
https://github.com/CircleCI-Public/slack-orb/wiki/Setup  
https://api.slack.com/apps 

https://bukialo.slack.com/marketplace/A0F7VRE7N-circleci

important invite bot to the channel
  

#####################################################

# On the runner machine, run as root or with sudo:
sudo usermod -aG docker circleci
# Or if using a different user:
sudo usermod -aG docker $(whoami)

# Check current permissions:
ls -la /var/run/docker.sock

# If needed, set proper permissions:
sudo chmod 666 /var/run/docker.sock
# Or better, ensure the docker group owns it:
sudo chown root:docker /var/run/docker.sock
sudo chmod 660 /var/run/docker.sock

# Restart the runner to pick up new group membership:
sudo systemctl restart circleci-runner
# Or if using a different service name/method, restart accordingly 
