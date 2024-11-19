{
  description = "Java Agent with Maven and ASM";

  inputs.nixpkgs.url = "nixpkgs/nixos-unstable";
  inputs.flake-utils.url = "github:numtide/flake-utils";

  outputs =
    {
      self,
      nixpkgs,
      flake-utils,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs { inherit system; };
      in
      {
        # Build the Java agent with Maven
        packages.default = pkgs.maven.buildMavenPackage {
          pname = "proxy-fix";
          version = "1.0";
          src = ./.;
          mvnHash = "sha256-6RIG79Inz7FrhX5/+t5KMrdcPqDPRffibeEK+2xecRc=";
        
          installPhase = ''
            mkdir -p $out/
            cp target/ProxyFix-1.0-SNAPSHOT-jar-with-dependencies.jar $out/
          '';
        };

        # Development shell with JDK 21 and Maven
        devShells.default = pkgs.mkShell {
          buildInputs = with pkgs; [
            jdk21
            maven
          ];
        };
      }
    );
}
