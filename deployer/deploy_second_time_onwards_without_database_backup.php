<?php
/*
 * Please change the configuration for correct use deploy.
 */

require 'recipe/common.php';
serverList('servers.yml');
// Set configurations
set('repository', 'https://abhijeet-infobeans:abhi1982@github.com/InternationalCodeCouncil/magentostore.git');  // Git Repository URL
set('shared_files', ['app/etc/local.xml','index.php','/app/code/local/Gorilla/StampPDF/Model/Shell.php','robots.txt']);
set('shared_dirs', ['var','media','sitemap','includes', 'all_csv']);
set('deploy_user', 'deployer');
set('writable_dir_user', 'apache');
set('root_user', 'root');
set('current_date', date('dMY'));
set('writable_use_sudo', true);
set('ssh_type', 'ext-ssh2');

/**
 * Set the Magento Recommended Permissions
 * Verified
 */
task('deploy:site:per', function () {
    $sudo = get('writable_use_sudo') ? 'sudo' : '';
    $root_user = get('root_user');
    $writable_dir_user = get('writable_dir_user');
    run("$sudo setfacl -R -d -m u:\"$root_user\":rwX,g:\"$root_user\":r-X {{release_path}}");
    run("$sudo setfacl -R -d -m u:\"$writable_dir_user\":rw-,g:\"$writable_dir_user\":rw- {{release_path}}/var/");
    run("$sudo setfacl -R -d -m u:\"$writable_dir_user\":rwX,g:\"$writable_dir_user\":rwx {{release_path}}/media/");
    run("$sudo setfacl -R -d -m u:\"$writable_dir_user\":rwX,g:\"$writable_dir_user\":--- {{release_path}}/sitemap/");
    run("$sudo setfacl -R -d -m u:\"$writable_dir_user\":rw-,g:\"$writable_dir_user\":rw- {{release_path}}/all_csv/");
    run("$sudo setfacl -R -d -m u:\"$root_user\":rwX,g:\"$root_user\":r-x {{release_path}}/includes/");
})->desc('Set the Magento Recommended Permissions');


/**
 * Put the site on maintenance
 * Verified
 */
task('deploy:site:maintenance', function () {
    run("cd {{deploy_path}}/current && touch maintenance.flag");
})->desc('Site on Maintenance');

/**
 * Database Backup
 * Verified
 */
task('deploy:database:backup', function () {
// Ask confirmation for database backup    
//$output  = askConfirmation("Want to take database backup?");
$output=false;
$current_date = get('current_date');
   if($output == true) {
       run("if [ ! -d {{database_backup_path}} ]; then mkdir -p {{database_backup_path}};fi");
       run ("mysqldump -h{{database_host}} -u{{database_user}} -p{{database_password}} {{database_name}} | gzip > {{database_backup_path}}/iccshop_db_bk_{{env}}$current_date.sql.gz");// Database backup path
   }
})->desc('Database backup');


/**
* Remove the site from maintenance
* Verified
*/
task('deploy:site:maintenance_remove', function () {
   run("cd {{deploy_path}}/current && rm -f maintenance.flag");
})->desc('Remove the site from Maintenance');

/**
 * Clear cache of shared directory
 */
task('deploy:cache:clear', function () {
    run("cd {{release_path}} && php -r \"require_once 'app/Mage.php'; umask(0); Mage::app()->cleanCache();\"");
})->desc('Clear cache');

/**
 * Disable Compilation
 * Verified
 */
task('deploy:disable_compiler', function () {
    run("cd {{release_path}} && php -f shell/compiler.php -- disable");
    run("cd {{release_path}} && php shell/compiler.php -- clear");
})->desc('Disable Compilation');

/**
 * Enable Compilation
 * Verified
 */
task('deploy:enable_compiler', function () {
    run("cd {{release_path}} && php -f shell/compiler.php -- enable");
    run("cd {{release_path}} && php -f shell/compiler.php -- compile");

    $deployUser = get('deploy_user');
    $chkUser = run("ls -l {{deploy_path}}/shared/includes/src | awk '{print $3}' | sort | uniq");
    if(trim($chkUser->getOutput()) != $deployUser) {
         writeln("Permission of Includes folder get chaged.Please change the permission from $chkUser to $deployUser");
            run("cd {{deploy_path}}/current && php -f shell/compiler.php -- disable");
            run("cd {{deploy_path}}/current && php -f shell/compiler.php -- clear");
            run("cd {{deploy_path}}/current && php -f shell/compiler.php -- enable");
            run("cd {{deploy_path}}/current && php shell/compiler.php -- compile");
    }
})->desc('Enable Compilation');


/**
 * Update project code
 */
task('deploy:update_code', function () {
    $repository = trim(get('repository'));
    $branch = env('branch');
    $git = env('bin/git');
    $gitVersion = run('{{bin/git}} version');
    $gitVersion = filter_var($gitVersion, FILTER_SANITIZE_NUMBER_FLOAT,FILTER_FLAG_ALLOW_FRACTION);
    $gitCache = env('git_cache');
    $depth = $gitCache ? '' : '--depth 1';
    $at = '';
    if (!empty($branch)) {
        $at = "-b $branch";
    }
    // If option `tag` is set
    if (input()->hasOption('tag')) {
        $tag = input()->getOption('tag');
        if (!empty($tag)) {
            $at = "-b $tag";
        }
    }
    // If option `tag` is not set and option `revision` is set
    if (empty($tag) && input()->hasOption('revision')) {
        $revision = input()->getOption('revision');
    }
    $releases = env('releases_list');
    if (!empty($revision)) {
        // To checkout specified revision we need to clone all tree.
        run("$git clone $at --recursive -q $repository {{release_path}} 2>&1");
        run("cd {{release_path}} && $git checkout $revision");
    } elseif ($gitCache && isset($releases[1])) {
        try {
            run("$git clone $at --recursive -q --reference {{deploy_path}}/releases/{$releases[1]} --dissociate $repository  {{release_path}} 2>&1");
        } catch (RuntimeException $exc) {
            // If {{deploy_path}}/releases/{$releases[1]} has a failed git clone, is empty, shallow etc, git would throw error and give up. So we're forcing it to act without reference in this situation
            run("$git clone $at --recursive -q $repository {{release_path}} 2>&1");
        }
    } else {
        // if we're using git cache this would be identical to above code in catch - full clone. If not, it would create shallow clone.
        run("$git clone $at $depth --recursive -q $repository {{release_path}} 2>&1");
        if($gitVersion < '1.9.1') {
          writeln("This step is for the older git version less than 1.9.1");
          run ("cd {{release_path}} && $git checkout $tag");
        }
    }
})->desc('Updating code');

after('deploy:update_code', 'deploy:disable_compiler');
after('deploy:cache:clear', 'deploy:enable_compiler');

/**
 * Main task
 */
task('deploy', [
    'deploy:site:maintenance',
    'deploy:database:backup',
    'deploy:prepare',
    'deploy:release',
    'deploy:update_code',
    'deploy:shared',
    'deploy:symlink',
    'deploy:cache:clear',
    'deploy:site:per',
    'cleanup',
    'deploy:site:maintenance_remove',
])->desc('Deploy your project');

after('deploy', 'success');


