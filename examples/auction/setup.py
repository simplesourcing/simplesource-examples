import setuptools

setuptools.setup(
    name='simplesource-auction-example',
    author="Simple Machines",
    description='Docker image tests',
    dependency_links=open("requirements.txt").read().split("\n"),
    include_package_data=True,
    python_requires='>=2.7',
)