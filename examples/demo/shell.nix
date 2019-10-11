with import (builtins.fetchTarball {
  name = "nixos-unstable-2019-11-10";
  # Commit hash for nixos-unstable as of 11 Nov 2019
  url = https://github.com/nixos/nixpkgs/archive/bef773ed53f3d535792d7d7ff3ea50a3deeb1cdd.tar.gz;
  # Hash obtained using `nix-prefetch-url --unpack <url>`
  sha256 = "0jhkh6gjdfwk398lkmzb16dgg6h6xyq5l7bh3sa3iw46byfk5i16";
}) {
  config = {
    packageOverrides = pkgs: with pkgs; {
      # Set default jre to Java 12
      java = pkgs.jdk12;
      jdk  = pkgs.jdk12;
      jre  = pkgs.jdk12;
    };
  };
};


let
  # Needed to override the nix AWS cli as we are using python37 in the shell with boto3.
  # If we do not override we get issues having python37 and python2 in the shell
  myAwscli = awscli.override { python = python37; };

  myPython = python37.withPackages (ps: with ps; [
    boto3
  ]);
in
stdenv.mkDerivation {
    name = "shell";
    buildInputs = [
      gradle
      jdk
      myAwscli
      myPython
    ];
}
