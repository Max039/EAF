### Changing the EvoAl Version
The current version of EvoAl can be changed via the **EvoAl** menu by selecting **Select Version**; however, only downloaded versions will be displayed.

### Managing EvoAl Versions
EvoAl versions can be managed manually under the **EvoAl** menu by selecting **Manage Versions**, which allows for the following actions:

- **Download the latest version of the "main branch"**
- **Download a specific version**
- **Delete outdated versions** (versions that are no longer available for download)
- **Open a downloaded version in File Explorer**
- **Delete a specific version**
- **Switch to the selected version**


### Manual Version Installation
- Download a all:package Artifact from [Pipelines](https://gitlab.informatik.uni-bremen.de/evoal/source/evoal-core/-/pipelines).
- Go to your local EAF_HOME
- In EAF_HOME/build create a new directory with a name for your evoal build
- Unzip the artifact and move the "evoal" directory inside the artifact into your directory in the builds directory
- The build will now be selectable under the tab EvoAl ... select version