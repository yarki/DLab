# DLab is Self-service, Fail-safe Exploratory Environment for Collaborative Data Science Workflow

## New features in v1.4

- Added new "Deep Learning" template (based on https://aws.amazon.com/marketplace/pp/B01M0AXXQB)
- Added ability for a data scientist to see billing information for usage of personal analytical environment (for notebook, EMR, storage)
- Added role based access feature. Depending on LDAP group, Data Scientists can now be granted access to specific analytical templates only
- All notebook servers now have GitWeb client and command line git client pre-installed to enable collaboration within the team

## Improvements in v.1.4

- MongoDB password is now configurable
- It is now possible to authenticate into DLab by UID, CN and E-mail
- Only specific version of analytical tools are now being installed
- Kernels related to obsolete EMR spot instances are now automatically removed
- Docker templates, related to Edge node status checks, are now automatically deleted to save disk space

## Bug fixes in v.1.4

- Fixed a problem with Edge node showing Terminated status during recreate procedure
- Fixed a problem with nVidia drivers not being properly defined on RedHat for TensorFlow template
- Fixed a bug when user could define Spot bid percentage, greater than maximum allowed value
- Fixed a bug when Create button was disabled after Edge node was started
