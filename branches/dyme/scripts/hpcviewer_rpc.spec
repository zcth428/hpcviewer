
%define debug_package %{nil}

Name: hpctoolkit_hpcviewer
Version: 1.0.1_333
Release: 1
Summary: "hpcviewer is a user interface for analyzing a database of performance metrics in conjunction with an application's source code." 
License: BSD like. See http://hpctoolkit.org/README.License or /usr/libexec/hpcviewer/doc/README.license
Distribution: hpcviewer
URL: http://hpctoolkit.org/ 
Group: System Environment/Base

Source: %{name}-%{version}-%{release}.tar.bz2
BuildRoot: %{_topdir}/BUILDROOT/%{name}-%{version}-%{release}.%{arch}
Requires: java >= 1.6.0

%description
HPCToolkit provides the hpcviewer browser for interactive examination of performance databases. 
The viewer can be launched from a command line (Linux/Unix platform) by entering hpcviewer.

This package requires that ant and java-devel be available in order to be able to build it.
No additional adjustments are required if they are installed via RPMs or are properly
configured to run from wherever they are located. Otherwise, the following rpmbuild 
command line options may be needed:

--define="JAVA_HOME /correct/path"
   The path that will be used for the JAVA_HOME environment variable.
   Usually the path to the bin directory that contains java and javac

--define="ANT_HOME /correct/path"
   The path that will be used for the ANT_HOME environment variable

--define="ANT_PATH /correct/path"
  The path directly to the ant executable

%undefine _missing_build_ids_terminate_build

%prep
%setup -n %{name}

%build
%if 0%{?JAVA_HOME:1}
	if [ -n "${JAVA_HOME+x}" ]; then
		echo "WARNING: JAVA_HOME is already defind as \"$JAVA_HOME\" and is being replaced from the command line by \"%{JAVA_HOME}\""
	fi
	JAVA_HOME=%{JAVA_HOME}
	export JAVA_HOME
	PATH=$JAVA_HOME/bin:$PATH
%endif

%if 0%{?ANT_HOME:1}
        if [ -n "${ANT_HOME+x}" ]; then
                echo "WARNING: ANT_HOME is already defind as \"$ANT_HOME\" and is being replaced from the command line by \"%{ANT_HOME}\""
        fi
        ANT_HOME=%{ANT_HOME}
        export ANT_HOME
%endif

%if 0%{?ANT_PATH:1}
        PATH=%{ANT_PATH}:$PATH
%endif
export PATH

cd ./scripts
ant -f buildViewer.xml
cd ..

%install
%{__rm} -rf %{buildroot}
%{__install} -d -m 755 %{buildroot}/usr/libexec/hpcviewer/doc
%{__install} -d -m 755 %{buildroot}/usr/bin
%{__cp} -pr hpcviewer/* %{buildroot}/usr/libexec/hpcviewer
%{__cp} -pr doc/* %{buildroot}/usr/libexec/hpcviewer/doc
%{__cp} -pr scripts/README.License %{buildroot}/usr/libexec/hpcviewer/doc
%{__chmod} 755  %{buildroot}/usr/libexec/hpcviewer/hpcviewer
%{__install} -p -m 755 scripts/hpcviewer.sh %{buildroot}/usr/bin/hpcviewer

%clean
%{__rm} -rf %{buildroot}

%files
%defattr(-,root,root,0755)
/usr/bin/hpcviewer
/usr/libexec/hpcviewer/configuration/config.ini
%docdir /usr/libexec/hpcviewer/doc
/usr/libexec/hpcviewer/doc/*
/usr/libexec/hpcviewer/plugins/*
/usr/libexec/hpcviewer/hpcviewer
/usr/libexec/hpcviewer/hpcviewer.ini

%postun
#if doing an uninstall and not an update
if [ $1 -eq 0 ] ; then
  #remove hpcviewer directory if it exists
  if [ -d /usr/libexec/hpcviewer ] ; then
    %{__rm} -rf /usr/libexec/hpcviewer
  fi
fi


%changelog
* Thu Sep 24 2009 ................
- Initial release of Rice's hpcviewer as RPM
