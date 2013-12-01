echo "Setting up permissions on local installation"
chgrp -R home /usr/local/lib/home-manager/HomeManagerNew
chmod -R g+w /usr/local/lib/home-manager/HomeManagerNew
chmod -R a+w /usr/local/lib/home-manager/HomeManagerNew
mv /usr/local/lib/home-manager/HomeManagerNew /usr/local/lib/home-manager/HomeManagerNew2
mv /usr/local/lib/home-manager/HomeManagerNew2/HomeManagerNew /usr/local/lib/home-manager/HomeManagerNew
rm -rf /usr/local/lib/home-manager/HomeManagerNew2