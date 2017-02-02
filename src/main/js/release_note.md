
local_proxy is added a submodule.

On cutting a new release, we shall build the proper dist/ files and remove unnecessary dependencies in node_modules under local_proxy.
A hackish way to do it right now is:

cd local_proxy
make release
